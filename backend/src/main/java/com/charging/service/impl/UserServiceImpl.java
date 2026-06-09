package com.charging.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.charging.common.ResultCode;
import com.charging.entity.UserInfo;
import com.charging.exception.BusinessException;
import com.charging.mapper.UserInfoMapper;
import com.charging.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserService {

    @Override
    public UserInfo getById(Long id) {
        UserInfo user = super.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }

    @Override
    public Page<UserInfo> page(String phone, Integer status, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        if (phone != null) {
            wrapper.like(UserInfo::getPhone, phone);
        }
        if (status != null) {
            wrapper.eq(UserInfo::getStatus, status);
        }
        wrapper.orderByDesc(UserInfo::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(UserInfo user) {
        user.setStatus(1);
        if (user.getBalance() == null) {
            user.setBalance(BigDecimal.ZERO);
        }
        return saveOrUpdate(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        UserInfo user = getById(id);
        user.setStatus(status);
        updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recharge(Long id, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("充值金额必须大于0");
        }
        UserInfo user = getById(id);
        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        user.setBalance(user.getBalance().add(amount));
        updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductBalance(Long id, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("扣款金额必须大于0");
        }
        UserInfo user = getById(id);
        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        if (user.getBalance().compareTo(amount) < 0) {
            throw new BusinessException(ResultCode.BALANCE_NOT_ENOUGH);
        }
        user.setBalance(user.getBalance().subtract(amount));
        updateById(user);
    }
}
