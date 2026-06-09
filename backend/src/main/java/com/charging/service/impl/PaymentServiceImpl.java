package com.charging.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.charging.common.ResultCode;
import com.charging.dto.PaymentDTO;
import com.charging.entity.ChargingRecord;
import com.charging.entity.PaymentRecord;
import com.charging.entity.UserInfo;
import com.charging.exception.BusinessException;
import com.charging.mapper.ChargingRecordMapper;
import com.charging.mapper.PaymentRecordMapper;
import com.charging.mapper.UserInfoMapper;
import com.charging.service.PaymentService;
import com.charging.service.UserService;
import com.charging.util.OrderNoUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付服务实现类
 */
@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentRecordMapper, PaymentRecord> implements PaymentService {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private ChargingRecordMapper chargingRecordMapper;

    @Resource
    private UserService userService;

    @Resource
    private com.charging.service.ChargingRecordService chargingRecordService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentRecord pay(PaymentDTO dto) {
        UserInfo user = userInfoMapper.selectById(dto.getUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        ChargingRecord record = chargingRecordMapper.selectById(dto.getChargingRecordId());
        if (record == null) {
            throw new BusinessException(ResultCode.CHARGING_NOT_FOUND);
        }
        if (record.getPaymentStatus() == 1) {
            throw new BusinessException("该订单已支付");
        }

        if (dto.getAmount().compareTo(record.getTotalAmount()) != 0) {
            throw new BusinessException("支付金额与订单金额不一致");
        }

        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setOrderNo(OrderNoUtil.generatePayOrderNo());
        paymentRecord.setUserId(dto.getUserId());
        paymentRecord.setChargingRecordId(dto.getChargingRecordId());
        paymentRecord.setAmount(dto.getAmount());
        paymentRecord.setPaymentMethod(dto.getPaymentMethod());
        paymentRecord.setPaymentStatus(0);
        paymentRecord.setRemark(dto.getRemark());
        save(paymentRecord);

        if (dto.getPaymentMethod() == 1) {
            userService.deductBalance(dto.getUserId(), dto.getAmount());
        }

        paymentRecord.setPaymentStatus(1);
        paymentRecord.setPayTime(LocalDateTime.now());
        paymentRecord.setTransactionId(OrderNoUtil.generatePayOrderNo());
        updateById(paymentRecord);

        chargingRecordService.updatePaymentStatus(
                dto.getChargingRecordId(),
                1,
                dto.getPaymentMethod(),
                dto.getAmount()
        );

        return paymentRecord;
    }

    @Override
    public PaymentRecord getDetail(Long id) {
        PaymentRecord record = getById(id);
        if (record == null) {
            throw new BusinessException(ResultCode.PAYMENT_NOT_FOUND);
        }
        return record;
    }

    @Override
    public Page<PaymentRecord> page(Long userId, Integer status, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<PaymentRecord> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(PaymentRecord::getUserId, userId);
        }
        if (status != null) {
            wrapper.eq(PaymentRecord::getPaymentStatus, status);
        }
        wrapper.orderByDesc(PaymentRecord::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refund(Long id, String reason) {
        PaymentRecord record = getById(id);
        if (record == null) {
            throw new BusinessException(ResultCode.PAYMENT_NOT_FOUND);
        }
        if (record.getPaymentStatus() != 1) {
            throw new BusinessException("只有已支付的订单才能退款");
        }

        UserInfo user = userInfoMapper.selectById(record.getUserId());
        if (user != null) {
            user.setBalance(user.getBalance().add(record.getAmount()));
            userInfoMapper.updateById(user);
        }

        record.setPaymentStatus(3);
        record.setRefundTime(LocalDateTime.now());
        record.setRefundAmount(record.getAmount());
        record.setRemark(reason);
        updateById(record);

        chargingRecordService.updatePaymentStatus(
                record.getChargingRecordId(),
                2,
                record.getPaymentMethod(),
                BigDecimal.ZERO
        );
    }
}
