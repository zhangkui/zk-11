package com.charging.service.impl;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.charging.common.Constants;
import com.charging.dto.ReservationCancelDTO;
import com.charging.dto.ReservationCreateDTO;
import com.charging.entity.ChargingPile;
import com.charging.entity.ChargingReservation;
import com.charging.entity.ChargingStation;
import com.charging.exception.BusinessException;
import com.charging.mapper.ChargingPileMapper;
import com.charging.mapper.ChargingReservationMapper;
import com.charging.mapper.ChargingStationMapper;
import com.charging.service.ChargingReservationService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class ChargingReservationServiceImpl extends ServiceImpl<ChargingReservationMapper, ChargingReservation> implements ChargingReservationService {

    @Resource
    private ChargingStationMapper chargingStationMapper;

    @Resource
    private ChargingPileMapper chargingPileMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${charging.queue.timeout-minutes:15}")
    private Integer timeoutMinutes;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChargingReservation create(ReservationCreateDTO dto) {
        if (dto.getReserveEndTime().isBefore(dto.getReserveStartTime())) {
            throw new BusinessException("预约结束时间不能早于开始时间");
        }

        if (dto.getReserveStartTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("预约开始时间不能早于当前时间");
        }

        long diffHours = DateUtil.between(DateUtil.date(dto.getReserveStartTime()), DateUtil.date(dto.getReserveEndTime()), DateUnit.HOUR, true);
        if (diffHours > 8) {
            throw new BusinessException("预约时长不能超过8小时");
        }

        ChargingStation station = chargingStationMapper.selectById(dto.getStationId());
        if (station == null) {
            throw new BusinessException("站点不存在");
        }
        if (station.getStatus() != 1) {
            throw new BusinessException("站点已停用，无法预约");
        }

        ChargingPile pile = null;
        if (dto.getPileId() != null) {
            pile = chargingPileMapper.selectById(dto.getPileId());
            if (pile == null) {
                throw new BusinessException("充电桩不存在");
            }
            if (!pile.getStationId().equals(dto.getStationId())) {
                throw new BusinessException("充电桩不属于该站点");
            }
            if (pile.getStatus() == Constants.PileStatus.IN_USE) {
                throw new BusinessException("充电桩已占用，无法预约");
            }
            if (pile.getStatus() == Constants.PileStatus.RESERVED) {
                throw new BusinessException("充电桩已被预约，无法预约");
            }
            if (pile.getStatus() == Constants.PileStatus.FAULT) {
                throw new BusinessException("充电桩故障，无法预约");
            }
            if (pile.getStatus() == Constants.PileStatus.MAINTENANCE) {
                throw new BusinessException("充电桩维护中，无法预约");
            }
        } else {
            LambdaQueryWrapper<ChargingPile> pileWrapper = new LambdaQueryWrapper<>();
            pileWrapper.eq(ChargingPile::getStationId, dto.getStationId());
            pileWrapper.eq(ChargingPile::getStatus, Constants.PileStatus.IDLE);
            pileWrapper.last("LIMIT 1");
            pile = chargingPileMapper.selectOne(pileWrapper);
            if (pile == null) {
                throw new BusinessException("该站点暂无空闲充电桩");
            }
        }

        List<ChargingReservation> conflicts = this.baseMapper.findConflictingReservations(
                dto.getStationId(), dto.getReserveStartTime(), dto.getReserveEndTime());

        final Long currentPileId = pile.getId();
        long count = conflicts.stream()
                .filter(r -> r.getPileId().equals(currentPileId))
                .count();
        if (count > 0) {
            throw new BusinessException("该充电桩在所选时段已被预约");
        }

        ChargingReservation reservation = new ChargingReservation();
        reservation.setReservationNo("RES" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        reservation.setUserId(dto.getUserId());
        reservation.setStationId(dto.getStationId());
        reservation.setPileId(pile.getId());
        reservation.setReserveStartTime(dto.getReserveStartTime());
        reservation.setReserveEndTime(dto.getReserveEndTime());
        reservation.setStatus(Constants.ReservationStatus.CONFIRMED);
        reservation.setRemark(dto.getRemark());
        this.save(reservation);

        pile.setStatus(Constants.PileStatus.RESERVED);
        pile.setCurrentOrderNo(reservation.getReservationNo());
        chargingPileMapper.updateById(pile);

        String redisKey = Constants.REDIS_RESERVATION_KEY + reservation.getReservationNo();
        redisTemplate.opsForValue().set(redisKey, reservation, timeoutMinutes + 30, TimeUnit.MINUTES);

        return reservation;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(ReservationCancelDTO dto) {
        ChargingReservation reservation = this.getById(dto.getId());
        if (reservation == null) {
            throw new BusinessException("预约记录不存在");
        }

        if (reservation.getStatus() != Constants.ReservationStatus.PENDING
                && reservation.getStatus() != Constants.ReservationStatus.CONFIRMED) {
            throw new BusinessException("当前状态不支持取消");
        }

        reservation.setStatus(Constants.ReservationStatus.CANCELLED);
        reservation.setCancelTime(LocalDateTime.now());
        reservation.setCancelReason(dto.getCancelReason());
        this.updateById(reservation);

        if (reservation.getPileId() != null) {
            ChargingPile pile = chargingPileMapper.selectById(reservation.getPileId());
            if (pile != null && pile.getStatus() == Constants.PileStatus.RESERVED) {
                pile.setStatus(Constants.PileStatus.IDLE);
                pile.setCurrentOrderNo(null);
                chargingPileMapper.updateById(pile);
            }
        }

        String redisKey = Constants.REDIS_RESERVATION_KEY + reservation.getReservationNo();
        redisTemplate.delete(redisKey);
    }

    @Override
    public IPage<ChargingReservation> pageByUser(Long userId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<ChargingReservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChargingReservation::getUserId, userId);
        wrapper.orderByDesc(ChargingReservation::getCreateTime);
        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmArrive(Long id) {
        ChargingReservation reservation = this.getById(id);
        if (reservation == null) {
            throw new BusinessException("预约记录不存在");
        }

        if (reservation.getStatus() != Constants.ReservationStatus.CONFIRMED) {
            throw new BusinessException("当前状态不支持确认到店");
        }

        reservation.setStatus(Constants.ReservationStatus.IN_PROGRESS);
        reservation.setArriveTime(LocalDateTime.now());
        this.updateById(reservation);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseTimeoutReservations() {
        LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(timeoutMinutes);
        List<ChargingReservation> timeoutReservations = this.baseMapper.findTimeoutReservations(timeoutTime);

        for (ChargingReservation reservation : timeoutReservations) {
            reservation.setStatus(Constants.ReservationStatus.TIMEOUT);
            this.updateById(reservation);

            if (reservation.getPileId() != null) {
                ChargingPile pile = chargingPileMapper.selectById(reservation.getPileId());
                if (pile != null && pile.getStatus() == Constants.PileStatus.RESERVED) {
                    pile.setStatus(Constants.PileStatus.IDLE);
                    pile.setCurrentOrderNo(null);
                    chargingPileMapper.updateById(pile);
                }
            }

            String redisKey = Constants.REDIS_RESERVATION_KEY + reservation.getReservationNo();
            redisTemplate.delete(redisKey);
        }
    }

    @Override
    public ChargingReservation getByNo(String reservationNo) {
        LambdaQueryWrapper<ChargingReservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChargingReservation::getReservationNo, reservationNo);
        return this.getOne(wrapper);
    }
}
