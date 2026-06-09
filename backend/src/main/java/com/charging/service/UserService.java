package com.charging.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.entity.UserInfo;

import java.math.BigDecimal;

/**
 * 用户服务接口
 */
public interface UserService {

    UserInfo getById(Long id);

    Page<UserInfo> page(String phone, Integer status, Integer pageNum, Integer pageSize);

    boolean save(UserInfo user);

    void updateStatus(Long id, Integer status);

    void recharge(Long id, BigDecimal amount);

    void deductBalance(Long id, BigDecimal amount);
}
