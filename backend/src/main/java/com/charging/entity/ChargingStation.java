package com.charging.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 充电站点实体类
 */
@Data
@TableName("charging_station")
public class ChargingStation implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String address;

    private Integer totalPiles;

    private Integer availablePiles;

    private BigDecimal pricePerKwh;

    private BigDecimal serviceFee;

    private Integer status;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
