package com.charging.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.charging.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("charging_fee")
public class ChargingFee extends BaseEntity {

    private String feeNo;

    private Long userId;

    private Long recordId;

    private BigDecimal totalAmount;

    private BigDecimal electricityFee;

    private BigDecimal serviceFee;

    private BigDecimal penaltyFee;

    private BigDecimal discountAmount;

    private BigDecimal payAmount;

    private Integer payStatus;

    private LocalDateTime payTime;

    private Integer payMethod;

    private BigDecimal pricePerKwh;

    private BigDecimal serviceFeePerKwh;

    private BigDecimal chargedKwh;

    private String remark;
}
