package com.charging.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.charging.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("charging_record")
public class ChargingRecord extends BaseEntity {

    private String recordNo;

    private Long userId;

    private Long stationId;

    private Long pileId;

    private Long reservationId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private BigDecimal startSoc;

    private BigDecimal endSoc;

    private BigDecimal chargedKwh;

    private Integer duration;

    private Integer status;

    private String stopReason;
}
