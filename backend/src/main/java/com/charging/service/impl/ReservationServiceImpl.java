package com.charging.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.charging.common.ResultCode;
import com.charging.config.ChargingProperties;
import com.charging.dto.ReservationCreateDTO;
import com.charging.entity.ChargingPile;
import com.charging.entity.ChargingStation;
import com.charging.entity.Reservation;
import com.charging.entity.UserInfo;
import com.charging.exception.BusinessException;
import com.charging.mapper.ChargingPileMapper;
import com.charging.mapper.ChargingStationMapper;
import com.charging.mapper.ReservationMapper;
import com.charging.mapper.UserInfoMapper;
import com.charging.service.ReservationService;
import com.charging.util.RedisKeyUtil;
import com.charging.vo.ReservationVO;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 预约服务实现类
 */
@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {

    @Resource
    private ChargingStationMapper chargingStationMapper;

    @Resource
    private ChargingPileMapper chargingPileMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ChargingProperties chargingProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Reservation create(ReservationCreateDTO dto) {
        UserInfo user = userInfoMapper.selectById(dto.getUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        ChargingStation station = chargingStationMapper.selectById(dto.getStationId());
        if (station == null) {
            throw new BusinessException(ResultCode.STATION_NOT_FOUND);
        }
        if (station.getStatus() != 1) {
            throw new BusinessException(ResultCode.STATION_CLOSED);
        }

        if (dto.getTimeSlot() < 1 || dto.getTimeSlot() > chargingProperties.getTimeSlots()) {
            throw new BusinessException("时段必须在1-" + chargingProperties.getTimeSlots() + "之间");
        }

        String key = RedisKeyUtil.getTimeSlotKey(dto.getStationId(), dto.getReservationDate(), dto.getTimeSlot());
        Integer count = (Integer) redisTemplate.opsForValue().get(key);
        if (count == null) {
            count = getReservationCount(dto.getStationId(), dto.getReservationDate(), dto.getTimeSlot());
        }

        if (count >= station.getTotalPiles()) {
            throw new BusinessException(ResultCode.TIME_SLOT_OCCUPIED);
        }

        ChargingPile pile = null;
        if (dto.getPileId() != null) {
            pile = chargingPileMapper.selectById(dto.getPileId());
            if (pile == null) {
                throw new BusinessException(ResultCode.PILE_NOT_FOUND);
            }
            if (pile.getStatus() != 0) {
                throw new BusinessException(ResultCode.PILE_NOT_AVAILABLE);
            }
        } else {
            LambdaQueryWrapper<ChargingPile> pileWrapper = new LambdaQueryWrapper<>();
            pileWrapper.eq(ChargingPile::getStationId, dto.getStationId());
            pileWrapper.eq(ChargingPile::getStatus, 0);
            pileWrapper.last("LIMIT 1");
            pile = chargingPileMapper.selectOne(pileWrapper);
            if (pile == null) {
                throw new BusinessException(ResultCode.PILE_NOT_AVAILABLE);
            }
        }

        Reservation reservation = new Reservation();
        reservation.setUserId(dto.getUserId());
        reservation.setStationId(dto.getStationId());
        reservation.setPileId(pile.getId());
        reservation.setReservationDate(dto.getReservationDate());
        reservation.setTimeSlot(dto.getTimeSlot());
        reservation.setCarPlate(dto.getCarPlate());
        reservation.setStatus(0);
        reservation.setExpireTime(LocalDateTime.now().plusMinutes(chargingProperties.getReservation().getTimeOutMinutes()));

        save(reservation);

        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, 1, TimeUnit.HOURS);

        pile.setStatus(2);
        chargingPileMapper.updateById(pile);

        return reservation;
    }

    @Override
    public ReservationVO getDetail(Long id) {
        Reservation reservation = getById(id);
        if (reservation == null) {
            throw new BusinessException(ResultCode.RESERVATION_NOT_FOUND);
        }
        return convertToVO(reservation);
    }

    @Override
    public Page<ReservationVO> page(Long userId, Long stationId, Integer status, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(Reservation::getUserId, userId);
        }
        if (stationId != null) {
            wrapper.eq(Reservation::getStationId, stationId);
        }
        if (status != null) {
            wrapper.eq(Reservation::getStatus, status);
        }
        wrapper.orderByDesc(Reservation::getCreateTime);

        Page<Reservation> page = page(new Page<>(pageNum, pageSize), wrapper);

        Page<ReservationVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<ReservationVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .toList();
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        Reservation reservation = getById(id);
        if (reservation == null) {
            throw new BusinessException(ResultCode.RESERVATION_NOT_FOUND);
        }
        if (reservation.getStatus() == 2) {
            throw new BusinessException(ResultCode.RESERVATION_CANCELLED);
        }
        if (reservation.getStatus() == 4) {
            throw new BusinessException(ResultCode.RESERVATION_COMPLETED);
        }

        reservation.setStatus(2);
        reservation.setCancelTime(LocalDateTime.now());
        updateById(reservation);

        if (reservation.getPileId() != null) {
            ChargingPile pile = chargingPileMapper.selectById(reservation.getPileId());
            if (pile != null && pile.getStatus() == 2) {
                pile.setStatus(0);
                chargingPileMapper.updateById(pile);
            }
        }

        String key = RedisKeyUtil.getTimeSlotKey(reservation.getStationId(), reservation.getReservationDate(), reservation.getTimeSlot());
        redisTemplate.opsForValue().decrement(key);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkIn(Long id) {
        Reservation reservation = getById(id);
        if (reservation == null) {
            throw new BusinessException(ResultCode.RESERVATION_NOT_FOUND);
        }
        if (reservation.getStatus() == 3) {
            throw new BusinessException(ResultCode.RESERVATION_TIMEOUT);
        }
        if (reservation.getExpireTime() != null && reservation.getExpireTime().isBefore(LocalDateTime.now())) {
            reservation.setStatus(3);
            updateById(reservation);
            throw new BusinessException(ResultCode.RESERVATION_TIMEOUT);
        }

        reservation.setStatus(1);
        reservation.setCheckInTime(LocalDateTime.now());
        updateById(reservation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long id) {
        Reservation reservation = getById(id);
        if (reservation == null) {
            throw new BusinessException(ResultCode.RESERVATION_NOT_FOUND);
        }

        reservation.setStatus(4);
        updateById(reservation);

        if (reservation.getPileId() != null) {
            ChargingPile pile = chargingPileMapper.selectById(reservation.getPileId());
            if (pile != null) {
                pile.setStatus(0);
                chargingPileMapper.updateById(pile);
            }
        }

        String key = RedisKeyUtil.getTimeSlotKey(reservation.getStationId(), reservation.getReservationDate(), reservation.getTimeSlot());
        redisTemplate.opsForValue().decrement(key);
    }

    @Override
    public List<Reservation> getTimeoutReservations() {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Reservation::getStatus, 0, 1);
        wrapper.isNotNull(Reservation::getExpireTime);
        wrapper.lt(Reservation::getExpireTime, LocalDateTime.now());
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseTimeoutReservation(Reservation reservation) {
        reservation.setStatus(3);
        updateById(reservation);

        if (reservation.getPileId() != null) {
            ChargingPile pile = chargingPileMapper.selectById(reservation.getPileId());
            if (pile != null && pile.getStatus() == 2) {
                pile.setStatus(0);
                chargingPileMapper.updateById(pile);
            }
        }

        String key = RedisKeyUtil.getTimeSlotKey(reservation.getStationId(), reservation.getReservationDate(), reservation.getTimeSlot());
        redisTemplate.opsForValue().decrement(key);
    }

    private int getReservationCount(Long stationId, java.time.LocalDate date, Integer timeSlot) {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getStationId, stationId);
        wrapper.eq(Reservation::getReservationDate, date);
        wrapper.eq(Reservation::getTimeSlot, timeSlot);
        wrapper.in(Reservation::getStatus, 0, 1);
        Long count = baseMapper.selectCount(wrapper);
        return count != null ? count.intValue() : 0;
    }

    private ReservationVO convertToVO(Reservation reservation) {
        ReservationVO vo = new ReservationVO();
        vo.setId(reservation.getId());
        vo.setUserId(reservation.getUserId());
        vo.setStationId(reservation.getStationId());
        vo.setPileId(reservation.getPileId());
        vo.setReservationDate(reservation.getReservationDate());
        vo.setTimeSlot(reservation.getTimeSlot());
        vo.setTimeSlotDesc(getTimeSlotDesc(reservation.getTimeSlot()));
        vo.setCarPlate(reservation.getCarPlate());
        vo.setStatus(reservation.getStatus());
        vo.setStatusDesc(getStatusDesc(reservation.getStatus()));
        vo.setQueueNumber(reservation.getQueueNumber());
        vo.setExpireTime(reservation.getExpireTime());
        vo.setCheckInTime(reservation.getCheckInTime());
        vo.setCreateTime(reservation.getCreateTime());

        UserInfo user = userInfoMapper.selectById(reservation.getUserId());
        if (user != null) {
            vo.setUserName(user.getNickname());
        }

        ChargingStation station = chargingStationMapper.selectById(reservation.getStationId());
        if (station != null) {
            vo.setStationName(station.getName());
        }

        if (reservation.getPileId() != null) {
            ChargingPile pile = chargingPileMapper.selectById(reservation.getPileId());
            if (pile != null) {
                vo.setPileNumber(pile.getPileNumber());
            }
        }

        return vo;
    }

    private String getTimeSlotDesc(Integer timeSlot) {
        int startHour = timeSlot - 1;
        int endHour = timeSlot;
        LocalTime startTime = LocalTime.of(startHour, 0);
        LocalTime endTime = LocalTime.of(endHour % 24, 0);
        return startTime + "-" + endTime;
    }

    private String getStatusDesc(Integer status) {
        return switch (status) {
            case 0 -> "待确认";
            case 1 -> "已确认";
            case 2 -> "已取消";
            case 3 -> "已超时";
            case 4 -> "已完成";
            default -> "未知";
        };
    }
}
