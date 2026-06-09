package com.charging.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.charging.common.ResultCode;
import com.charging.config.ChargingProperties;
import com.charging.dto.StationQueryDTO;
import com.charging.dto.StationSaveDTO;
import com.charging.entity.ChargingPile;
import com.charging.entity.ChargingStation;
import com.charging.entity.Reservation;
import com.charging.exception.BusinessException;
import com.charging.mapper.ChargingPileMapper;
import com.charging.mapper.ChargingStationMapper;
import com.charging.mapper.ReservationMapper;
import com.charging.service.ChargingStationService;
import com.charging.util.RedisKeyUtil;
import com.charging.vo.StationDetailVO;
import com.charging.vo.TimeSlotStatusVO;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 充电站服务实现类
 */
@Service
public class ChargingStationServiceImpl extends ServiceImpl<ChargingStationMapper, ChargingStation> implements ChargingStationService {

    @Resource
    private ChargingPileMapper chargingPileMapper;

    @Resource
    private ReservationMapper reservationMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ChargingProperties chargingProperties;

    @Override
    public Page<ChargingStation> page(StationQueryDTO queryDTO) {
        LambdaQueryWrapper<ChargingStation> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO.getName() != null) {
            wrapper.like(ChargingStation::getName, queryDTO.getName());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(ChargingStation::getStatus, queryDTO.getStatus());
        }
        wrapper.orderByDesc(ChargingStation::getCreateTime);
        return page(new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);
    }

    @Override
    public StationDetailVO getDetail(Long id) {
        ChargingStation station = getById(id);
        if (station == null) {
            throw new BusinessException(ResultCode.STATION_NOT_FOUND);
        }

        StationDetailVO vo = new StationDetailVO();
        vo.setId(station.getId());
        vo.setName(station.getName());
        vo.setAddress(station.getAddress());
        vo.setTotalPiles(station.getTotalPiles());
        vo.setAvailablePiles(station.getAvailablePiles());
        vo.setPricePerKwh(station.getPricePerKwh());
        vo.setServiceFee(station.getServiceFee());
        vo.setStatus(station.getStatus());
        vo.setLongitude(station.getLongitude());
        vo.setLatitude(station.getLatitude());
        vo.setCreateTime(station.getCreateTime());

        LambdaQueryWrapper<ChargingPile> pileWrapper = new LambdaQueryWrapper<>();
        pileWrapper.eq(ChargingPile::getStationId, id);
        List<ChargingPile> piles = chargingPileMapper.selectList(pileWrapper);

        List<StationDetailVO.PileVO> pileVOs = new ArrayList<>();
        for (ChargingPile pile : piles) {
            StationDetailVO.PileVO pileVO = new StationDetailVO.PileVO();
            pileVO.setId(pile.getId());
            pileVO.setPileNumber(pile.getPileNumber());
            pileVO.setType(pile.getType());
            pileVO.setPower(pile.getPower());
            pileVO.setStatus(pile.getStatus());
            pileVOs.add(pileVO);
        }
        vo.setPiles(pileVOs);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(StationSaveDTO saveDTO) {
        ChargingStation station;
        if (saveDTO.getId() != null) {
            station = getById(saveDTO.getId());
            if (station == null) {
                throw new BusinessException(ResultCode.STATION_NOT_FOUND);
            }
        } else {
            station = new ChargingStation();
            station.setAvailablePiles(saveDTO.getTotalPiles());
        }

        station.setName(saveDTO.getName());
        station.setAddress(saveDTO.getAddress());
        station.setTotalPiles(saveDTO.getTotalPiles());
        station.setPricePerKwh(saveDTO.getPricePerKwh() != null ? saveDTO.getPricePerKwh() : BigDecimal.ZERO);
        station.setServiceFee(saveDTO.getServiceFee() != null ? saveDTO.getServiceFee() : BigDecimal.ZERO);
        station.setStatus(saveDTO.getStatus() != null ? saveDTO.getStatus() : 1);
        station.setLongitude(saveDTO.getLongitude());
        station.setLatitude(saveDTO.getLatitude());

        saveOrUpdate(station);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        ChargingStation station = getById(id);
        if (station == null) {
            throw new BusinessException(ResultCode.STATION_NOT_FOUND);
        }
        station.setStatus(status);
        updateById(station);
    }

    @Override
    public List<TimeSlotStatusVO> getTimeSlotStatus(Long stationId, LocalDate date) {
        ChargingStation station = getById(stationId);
        if (station == null) {
            throw new BusinessException(ResultCode.STATION_NOT_FOUND);
        }

        int timeSlots = chargingProperties.getTimeSlots();
        List<TimeSlotStatusVO> result = new ArrayList<>();

        for (int i = 1; i <= timeSlots; i++) {
            TimeSlotStatusVO vo = new TimeSlotStatusVO();
            vo.setTimeSlot(i);
            vo.setTimeSlotDesc(getTimeSlotDesc(i));
            vo.setTotalCount(station.getTotalPiles());

            String key = RedisKeyUtil.getTimeSlotKey(stationId, date, i);
            Integer count = (Integer) redisTemplate.opsForValue().get(key);
            if (count == null) {
                count = getReservationCountFromDb(stationId, date, i);
                redisTemplate.opsForValue().set(key, count, 1, TimeUnit.HOURS);
            }

            int available = station.getTotalPiles() - count;
            vo.setAvailableCount(Math.max(0, available));
            vo.setStatus(available > 0 ? 0 : 1);
            vo.setStatusDesc(available > 0 ? "可预约" : "已约满");

            result.add(vo);
        }

        return result;
    }

    @Override
    public boolean checkTimeSlotAvailable(Long stationId, LocalDate date, Integer timeSlot) {
        ChargingStation station = getById(stationId);
        if (station == null) {
            throw new BusinessException(ResultCode.STATION_NOT_FOUND);
        }
        if (station.getStatus() != 1) {
            throw new BusinessException(ResultCode.STATION_CLOSED);
        }

        String key = RedisKeyUtil.getTimeSlotKey(stationId, date, timeSlot);
        Integer count = (Integer) redisTemplate.opsForValue().get(key);
        if (count == null) {
            count = getReservationCountFromDb(stationId, date, timeSlot);
            redisTemplate.opsForValue().set(key, count, 1, TimeUnit.HOURS);
        }

        return count < station.getTotalPiles();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decreaseAvailablePile(Long stationId) {
        ChargingStation station = getById(stationId);
        if (station.getAvailablePiles() > 0) {
            station.setAvailablePiles(station.getAvailablePiles() - 1);
            updateById(station);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void increaseAvailablePile(Long stationId) {
        ChargingStation station = getById(stationId);
        if (station.getAvailablePiles() < station.getTotalPiles()) {
            station.setAvailablePiles(station.getAvailablePiles() + 1);
            updateById(station);
        }
    }

    private int getReservationCountFromDb(Long stationId, LocalDate date, Integer timeSlot) {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getStationId, stationId);
        wrapper.eq(Reservation::getReservationDate, date);
        wrapper.eq(Reservation::getTimeSlot, timeSlot);
        wrapper.in(Reservation::getStatus, 0, 1);
        Long count = reservationMapper.selectCount(wrapper);
        return count != null ? count.intValue() : 0;
    }

    private String getTimeSlotDesc(Integer timeSlot) {
        int startHour = timeSlot - 1;
        int endHour = timeSlot;
        LocalTime startTime = LocalTime.of(startHour, 0);
        LocalTime endTime = LocalTime.of(endHour % 24, 0);
        return startTime + "-" + endTime;
    }
}
