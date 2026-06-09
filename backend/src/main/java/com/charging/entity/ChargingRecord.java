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
 * 充电记录实体类
 */
@Data
@TableName("charging_record")
public class ChargingRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long stationId;

    private Long pileId;

    private Long reservationId;

    private String carPlate;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer duration;

    private BigDecimal startKwh;

    private BigDecimal endKwh;

    private BigDecimal chargedKwh;

    private BigDecimal pricePerKwh;

    private BigDecimal serviceFee;

    private BigDecimal totalAmount;

    private BigDecimal paidAmount;

    private Integer paymentStatus;

    private LocalDateTime paymentTime;

    private Integer paymentMethod;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
