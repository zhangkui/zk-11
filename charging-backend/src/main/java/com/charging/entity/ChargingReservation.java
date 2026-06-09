package com.charging.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.charging.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("charging_reservation")
public class ChargingReservation extends BaseEntity {

    private String reservationNo;

    private Long userId;

    private Long stationId;

    private Long pileId;

    private LocalDateTime reserveStartTime;

    private LocalDateTime reserveEndTime;

    private Integer status;

    private Integer queueNumber;

    private LocalDateTime arriveTime;

    private LocalDateTime cancelTime;

    private String cancelReason;

    private String remark;
}
