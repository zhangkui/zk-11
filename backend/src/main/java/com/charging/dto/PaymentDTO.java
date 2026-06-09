package com.charging.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 支付DTO
 */
@Data
public class PaymentDTO implements Serializable {

    @NotNull(message = "充电记录ID不能为空")
    private Long chargingRecordId;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "支付金额不能为空")
    private BigDecimal amount;

    @NotNull(message = "支付方式不能为空")
    private Integer paymentMethod;

    private String remark;
}
