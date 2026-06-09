package com.charging.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.dto.PaymentDTO;
import com.charging.entity.PaymentRecord;

/**
 * 支付服务接口
 */
public interface PaymentService {

    PaymentRecord pay(PaymentDTO dto);

    PaymentRecord getDetail(Long id);

    Page<PaymentRecord> page(Long userId, Integer status, Integer pageNum, Integer pageSize);

    void refund(Long id, String reason);
}
