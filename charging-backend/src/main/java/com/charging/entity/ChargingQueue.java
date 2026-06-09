package com.charging.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.charging.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("charging_queue")
public class ChargingQueue extends BaseEntity {

    private String queueNo;

    private Long userId;

    private Long stationId;

    private Integer queueNumber;

    private Integer status;

    private Integer pileType;

    private Integer estimatedWaitTime;

    private LocalDateTime calledTime;

    private LocalDateTime cancelTime;

    private LocalDateTime expireTime;

    private String remark;
}
