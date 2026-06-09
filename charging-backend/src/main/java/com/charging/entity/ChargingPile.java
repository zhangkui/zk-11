package com.charging.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.charging.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("charging_pile")
public class ChargingPile extends BaseEntity {

    private Long stationId;

    private String pileNo;

    private Integer pileType;

    private BigDecimal powerRating;

    private Integer status;

    private String currentOrderNo;
}
