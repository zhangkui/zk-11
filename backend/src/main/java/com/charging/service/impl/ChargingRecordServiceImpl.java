package com.charging.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.charging.common.ResultCode;
import com.charging.dto.ChargingEndDTO;
import com.charging.dto.ChargingStartDTO;
import com.charging.entity.*;
import com.charging.exception.BusinessException;
import com.charging.mapper.*;
import com.charging.service.ChargingRecordService;
import com.charging.vo.ChargingRecordVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 充电记录服务实现类
 */
@Service
public class ChargingRecordServiceImpl extends ServiceImpl<ChargingRecordMapper, ChargingRecord> implements ChargingRecordService {

    @Resource
    private ChargingPileMapper chargingPileMapper;

    @Resource
    private ChargingStationMapper chargingStationMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private ReservationMapper reservationMapper;

    @Resource
    private ChargingQueueMapper chargingQueueMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChargingRecord start(ChargingStartDTO dto) {
        UserInfo user = userInfoMapper.selectById(dto.getUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        ChargingPile pile = chargingPileMapper.selectById(dto.getPileId());
        if (pile == null) {
            throw new BusinessException(ResultCode.PILE_NOT_FOUND);
        }
        if (pile.getStatus() != 0 && pile.getStatus() != 2) {
            throw new BusinessException(ResultCode.PILE_NOT_AVAILABLE);
        }

        ChargingStation station = chargingStationMapper.selectById(pile.getStationId());
        if (station == null) {
            throw new BusinessException(ResultCode.STATION_NOT_FOUND);
        }

        if (dto.getReservationId() != null) {
            Reservation reservation = reservationMapper.selectById(dto.getReservationId());
            if (reservation != null) {
                reservation.setStatus(1);
                reservationMapper.updateById(reservation);
            }
        }

        if (dto.getQueueId() != null) {
            ChargingQueue queue = chargingQueueMapper.selectById(dto.getQueueId());
            if (queue != null && queue.getStatus() == 1) {
                queue.setStatus(2);
                chargingQueueMapper.updateById(queue);
            }
        }

        ChargingRecord record = new ChargingRecord();
        record.setUserId(dto.getUserId());
        record.setStationId(pile.getStationId());
        record.setPileId(dto.getPileId());
        record.setReservationId(dto.getReservationId());
        record.setCarPlate(user.getCarPlate());
        record.setStartTime(LocalDateTime.now());
        record.setStartKwh(dto.getStartKwh());
        record.setPricePerKwh(station.getPricePerKwh());
        record.setServiceFee(station.getServiceFee());
        record.setPaymentStatus(0);
        record.setStatus(0);

        save(record);

        pile.setStatus(1);
        pile.setCurrentOrderId(record.getId());
        chargingPileMapper.updateById(pile);

        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChargingRecord end(ChargingEndDTO dto) {
        ChargingRecord record = getById(dto.getRecordId());
        if (record == null) {
            throw new BusinessException(ResultCode.CHARGING_NOT_FOUND);
        }
        if (record.getStatus() == 1) {
            throw new BusinessException(ResultCode.CHARGING_COMPLETED);
        }

        LocalDateTime endTime = LocalDateTime.now();
        record.setEndTime(endTime);

        Duration duration = Duration.between(record.getStartTime(), endTime);
        record.setDuration((int) duration.toMinutes());

        BigDecimal chargedKwh = dto.getEndKwh().subtract(record.getStartKwh());
        if (chargedKwh.compareTo(BigDecimal.ZERO) < 0) {
            chargedKwh = BigDecimal.ZERO;
        }
        record.setEndKwh(dto.getEndKwh());
        record.setChargedKwh(chargedKwh);

        BigDecimal electricityFee = chargedKwh.multiply(record.getPricePerKwh());
        BigDecimal serviceFee = chargedKwh.multiply(record.getServiceFee());
        BigDecimal totalAmount = electricityFee.add(serviceFee);
        record.setTotalAmount(totalAmount);

        record.setStatus(1);
        updateById(record);

        ChargingPile pile = chargingPileMapper.selectById(record.getPileId());
        if (pile != null) {
            pile.setStatus(0);
            pile.setCurrentOrderId(null);
            chargingPileMapper.updateById(pile);
        }

        if (record.getReservationId() != null) {
            Reservation reservation = reservationMapper.selectById(record.getReservationId());
            if (reservation != null) {
                reservation.setStatus(4);
                reservationMapper.updateById(reservation);
            }
        }

        return record;
    }

    @Override
    public ChargingRecordVO getDetail(Long id) {
        ChargingRecord record = getById(id);
        if (record == null) {
            throw new BusinessException(ResultCode.CHARGING_NOT_FOUND);
        }
        return convertToVO(record);
    }

    @Override
    public Page<ChargingRecordVO> page(Long userId, Long stationId, Integer status, Integer paymentStatus, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<ChargingRecord> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(ChargingRecord::getUserId, userId);
        }
        if (stationId != null) {
            wrapper.eq(ChargingRecord::getStationId, stationId);
        }
        if (status != null) {
            wrapper.eq(ChargingRecord::getStatus, status);
        }
        if (paymentStatus != null) {
            wrapper.eq(ChargingRecord::getPaymentStatus, paymentStatus);
        }
        wrapper.orderByDesc(ChargingRecord::getCreateTime);

        Page<ChargingRecord> page = page(new Page<>(pageNum, pageSize), wrapper);

        Page<ChargingRecordVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<ChargingRecordVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .toList();
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePaymentStatus(Long id, Integer paymentStatus, Integer paymentMethod, BigDecimal paidAmount) {
        ChargingRecord record = getById(id);
        if (record == null) {
            throw new BusinessException(ResultCode.CHARGING_NOT_FOUND);
        }

        record.setPaymentStatus(paymentStatus);
        record.setPaymentMethod(paymentMethod);
        record.setPaidAmount(paidAmount);
        record.setPaymentTime(LocalDateTime.now());
        updateById(record);
    }

    private ChargingRecordVO convertToVO(ChargingRecord record) {
        ChargingRecordVO vo = new ChargingRecordVO();
        vo.setId(record.getId());
        vo.setUserId(record.getUserId());
        vo.setStationId(record.getStationId());
        vo.setPileId(record.getPileId());
        vo.setCarPlate(record.getCarPlate());
        vo.setStartTime(record.getStartTime());
        vo.setEndTime(record.getEndTime());
        vo.setDuration(record.getDuration());
        vo.setStartKwh(record.getStartKwh());
        vo.setEndKwh(record.getEndKwh());
        vo.setChargedKwh(record.getChargedKwh());
        vo.setPricePerKwh(record.getPricePerKwh());
        vo.setServiceFee(record.getServiceFee());
        vo.setTotalAmount(record.getTotalAmount());
        vo.setPaidAmount(record.getPaidAmount());
        vo.setPaymentStatus(record.getPaymentStatus());
        vo.setPaymentStatusDesc(getPaymentStatusDesc(record.getPaymentStatus()));
        vo.setStatus(record.getStatus());
        vo.setStatusDesc(getStatusDesc(record.getStatus()));
        vo.setCreateTime(record.getCreateTime());

        UserInfo user = userInfoMapper.selectById(record.getUserId());
        if (user != null) {
            vo.setUserName(user.getNickname());
        }

        ChargingStation station = chargingStationMapper.selectById(record.getStationId());
        if (station != null) {
            vo.setStationName(station.getName());
        }

        ChargingPile pile = chargingPileMapper.selectById(record.getPileId());
        if (pile != null) {
            vo.setPileNumber(pile.getPileNumber());
        }

        return vo;
    }

    private String getPaymentStatusDesc(Integer status) {
        return switch (status) {
            case 0 -> "未支付";
            case 1 -> "已支付";
            case 2 -> "已退款";
            default -> "未知";
        };
    }

    private String getStatusDesc(Integer status) {
        return switch (status) {
            case 0 -> "充电中";
            case 1 -> "已完成";
            case 2 -> "异常结束";
            default -> "未知";
        };
    }
}
