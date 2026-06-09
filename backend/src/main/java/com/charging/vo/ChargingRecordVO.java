package com.charging.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 充电记录VO
 */
@Data
public class ChargingRecordVO implements Serializable {

    private Long id;

    private Long userId;

    private String userName;

    private Long stationId;

    private String stationName;

    private Long pileId;

    private String pileNumber;

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

    private String paymentStatusDesc;

    private Integer status;

    private String statusDesc;

    private LocalDateTime createTime;
}
