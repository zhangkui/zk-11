package com.charging.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.charging.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("charging_station")
public class ChargingStation extends BaseEntity {

    private String name;

    private String address;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private Integer totalPiles;

    private Integer availablePiles;

    private Integer status;

    private String openingHours;

    private String phone;

    private String description;
}
