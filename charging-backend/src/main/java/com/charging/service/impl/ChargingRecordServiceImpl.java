package com.charging.service.impl;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.charging.common.Constants;
import com.charging.dto.ChargingEndDTO;
import com.charging.dto.ChargingStartDTO;
import com.charging.entity.ChargingPile;
import com.charging.entity.ChargingQueue;
import com.charging.entity.ChargingRecord;
import com.charging.entity.ChargingReservation;
import com.charging.entity.ChargingStation;
import com.charging.entity.User;
import com.charging.exception.BusinessException;
import com.charging.mapper.ChargingPileMapper;
import com.charging.mapper.ChargingQueueMapper;
import com.charging.mapper.ChargingRecordMapper;
import com.charging.mapper.ChargingReservationMapper;
import com.charging.mapper.ChargingStationMapper;
import com.charging.mapper.UserMapper;
import com.charging.service.ChargingFeeService;
import com.charging.service.ChargingRecordService;
import com.charging.service.ChargingStationService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ChargingRecordServiceImpl extends ServiceImpl<ChargingRecordMapper, ChargingRecord> implements ChargingRecordService {

    @Resource
    private ChargingStationMapper chargingStationMapper;

    @Resource
    private ChargingPileMapper chargingPileMapper;

    @Resource
    private ChargingReservationMapper chargingReservationMapper;

    @Resource
    private ChargingQueueMapper chargingQueueMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ChargingFeeService chargingFeeService;

    @Resource
    private ChargingStationService chargingStationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChargingRecord startCharging(ChargingStartDTO dto) {
        ChargingStation station = chargingStationMapper.selectById(dto.getStationId());
        if (station == null || station.getStatus() != 1) {
            throw new BusinessException("站点不存在或未启用");
        }

        ChargingPile pile = chargingPileMapper.selectById(dto.getPileId());
        if (pile == null) {
            throw new BusinessException("充电桩不存在");
        }
        if (!pile.getStationId().equals(dto.getStationId())) {
            throw new BusinessException("充电桩不属于该站点");
        }
        if (pile.getStatus() != Constants.PileStatus.IDLE && pile.getStatus() != Constants.PileStatus.RESERVED) {
            throw new BusinessException("充电桩当前不可用");
        }

        User user = userMapper.selectById(dto.getUserId());
        if (user == null || user.getStatus() != 1) {
            throw new BusinessException("用户不存在或已禁用");
        }

        if (dto.getReservationId() != null) {
            ChargingReservation reservation = chargingReservationMapper.selectById(dto.getReservationId());
            if (reservation == null) {
                throw new BusinessException("预约记录不存在");
            }
            if (reservation.getStatus() != Constants.ReservationStatus.CONFIRMED
                    && reservation.getStatus() != Constants.ReservationStatus.IN_PROGRESS) {
                throw new BusinessException("预约状态不正确");
            }
            if (!reservation.getPileId().equals(dto.getPileId())) {
                throw new BusinessException("预约充电桩不匹配");
            }
            if (reservation.getStatus() == Constants.ReservationStatus.CONFIRMED) {
                reservation.setStatus(Constants.ReservationStatus.IN_PROGRESS);
                reservation.setArriveTime(LocalDateTime.now());
                chargingReservationMapper.updateById(reservation);
            }
        }

        if (dto.getQueueId() != null) {
            ChargingQueue queue = chargingQueueMapper.selectById(dto.getQueueId());
            if (queue != null && queue.getStatus() == Constants.QueueStatus.CALLED) {
                queue.setStatus(Constants.QueueStatus.USED);
                chargingQueueMapper.updateById(queue);
            }
        }

        LambdaQueryWrapper<ChargingRecord> existingWrapper = new LambdaQueryWrapper<>();
        existingWrapper.eq(ChargingRecord::getUserId, dto.getUserId());
        existingWrapper.eq(ChargingRecord::getStatus, Constants.ChargingStatus.CHARGING);
        Long existingCount = this.baseMapper.selectCount(existingWrapper);
        if (existingCount > 0) {
            throw new BusinessException("您有正在进行中的充电订单，请先结束");
        }

        ChargingRecord record = new ChargingRecord();
        record.setRecordNo("REC" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        record.setUserId(dto.getUserId());
        record.setStationId(dto.getStationId());
        record.setPileId(dto.getPileId());
        record.setReservationId(dto.getReservationId());
        record.setStartTime(LocalDateTime.now());
        record.setStartSoc(dto.getStartSoc());
        record.setChargedKwh(BigDecimal.ZERO);
        record.setStatus(Constants.ChargingStatus.CHARGING);
        this.save(record);

        pile.setStatus(Constants.PileStatus.IN_USE);
        pile.setCurrentOrderNo(record.getRecordNo());
        chargingPileMapper.updateById(pile);

        chargingStationService.updateAvailablePiles(dto.getStationId());

        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChargingRecord endCharging(ChargingEndDTO dto) {
        ChargingRecord record = this.getById(dto.getRecordId());
        if (record == null) {
            throw new BusinessException("充电记录不存在");
        }
        if (record.getStatus() != Constants.ChargingStatus.CHARGING) {
            throw new BusinessException("当前充电订单状态不正确");
        }

        record.setEndTime(LocalDateTime.now());
        record.setEndSoc(dto.getEndSoc());
        record.setChargedKwh(dto.getChargedKwh());

        long duration = DateUtil.between(DateUtil.date(record.getStartTime()), DateUtil.date(record.getEndTime()), DateUnit.MINUTE, true);
        record.setDuration((int) duration);

        if (dto.getStatus() != null) {
            record.setStatus(dto.getStatus());
        } else {
            record.setStatus(Constants.ChargingStatus.COMPLETED);
        }
        record.setStopReason(dto.getStopReason());
        this.updateById(record);

        ChargingPile pile = chargingPileMapper.selectById(record.getPileId());
        if (pile != null) {
            pile.setStatus(Constants.PileStatus.IDLE);
            pile.setCurrentOrderNo(null);
            chargingPileMapper.updateById(pile);
        }

        if (record.getReservationId() != null) {
            ChargingReservation reservation = chargingReservationMapper.selectById(record.getReservationId());
            if (reservation != null && reservation.getStatus() == Constants.ReservationStatus.IN_PROGRESS) {
                reservation.setStatus(Constants.ReservationStatus.COMPLETED);
                chargingReservationMapper.updateById(reservation);
            }
        }

        chargingStationService.updateAvailablePiles(record.getStationId());

        chargingFeeService.createFee(record);

        return record;
    }

    @Override
    public IPage<ChargingRecord> pageByUser(Long userId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<ChargingRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChargingRecord::getUserId, userId);
        wrapper.orderByDesc(ChargingRecord::getCreateTime);
        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public ChargingRecord getCurrentCharging(Long userId, Long stationId) {
        LambdaQueryWrapper<ChargingRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChargingRecord::getUserId, userId);
        wrapper.eq(ChargingRecord::getStatus, Constants.ChargingStatus.CHARGING);
        if (stationId != null) {
            wrapper.eq(ChargingRecord::getStationId, stationId);
        }
        return this.getOne(wrapper);
    }
}
