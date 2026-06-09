package com.charging.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.charging.common.Constants;
import com.charging.dto.FeePayDTO;
import com.charging.entity.ChargingFee;
import com.charging.entity.ChargingRecord;
import com.charging.entity.User;
import com.charging.exception.BusinessException;
import com.charging.mapper.ChargingFeeMapper;
import com.charging.mapper.UserMapper;
import com.charging.service.ChargingFeeService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ChargingFeeServiceImpl extends ServiceImpl<ChargingFeeMapper, ChargingFee> implements ChargingFeeService {

    @Resource
    private UserMapper userMapper;

    @Value("${charging.fee.price-per-kwh:1.5}")
    private BigDecimal pricePerKwh;

    @Value("${charging.fee.service-fee-per-kwh:0.5}")
    private BigDecimal serviceFeePerKwh;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChargingFee createFee(ChargingRecord record) {
        BigDecimal chargedKwh = record.getChargedKwh();
        if (chargedKwh == null || chargedKwh.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("充电量不能为0");
        }

        BigDecimal electricityFee = chargedKwh.multiply(pricePerKwh);
        BigDecimal serviceFee = chargedKwh.multiply(serviceFeePerKwh);
        BigDecimal totalAmount = electricityFee.add(serviceFee);

        ChargingFee fee = new ChargingFee();
        fee.setFeeNo("FEE" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        fee.setUserId(record.getUserId());
        fee.setRecordId(record.getId());
        fee.setTotalAmount(totalAmount);
        fee.setElectricityFee(electricityFee);
        fee.setServiceFee(serviceFee);
        fee.setPenaltyFee(BigDecimal.ZERO);
        fee.setDiscountAmount(BigDecimal.ZERO);
        fee.setPayAmount(totalAmount);
        fee.setPayStatus(Constants.PayStatus.UNPAID);
        fee.setPricePerKwh(pricePerKwh);
        fee.setServiceFeePerKwh(serviceFeePerKwh);
        fee.setChargedKwh(chargedKwh);
        this.save(fee);

        return fee;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChargingFee pay(FeePayDTO dto) {
        ChargingFee fee = this.getById(dto.getFeeId());
        if (fee == null) {
            throw new BusinessException("费用记录不存在");
        }
        if (fee.getPayStatus() != Constants.PayStatus.UNPAID) {
            throw new BusinessException("当前订单状态不支持支付");
        }

        User user = userMapper.selectById(fee.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (dto.getPayMethod() == Constants.PayMethod.BALANCE) {
            if (user.getBalance().compareTo(fee.getPayAmount()) < 0) {
                throw new BusinessException("余额不足，请充值");
            }
            user.setBalance(user.getBalance().subtract(fee.getPayAmount()));
            userMapper.updateById(user);
        }

        fee.setPayStatus(Constants.PayStatus.PAID);
        fee.setPayTime(LocalDateTime.now());
        fee.setPayMethod(dto.getPayMethod());
        this.updateById(fee);

        return fee;
    }

    @Override
    public IPage<ChargingFee> pageByUser(Long userId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<ChargingFee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChargingFee::getUserId, userId);
        wrapper.orderByDesc(ChargingFee::getCreateTime);
        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public ChargingFee getByRecordId(Long recordId) {
        LambdaQueryWrapper<ChargingFee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChargingFee::getRecordId, recordId);
        return this.getOne(wrapper);
    }
}
