package com.charging.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QueueInfoVO {

    private Long id;

    private String queueNo;

    private Integer queueNumber;

    private Integer status;

    private String statusName;

    private Long stationId;

    private String stationName;

    private Integer pileType;

    private String pileTypeName;

    private Integer aheadCount;

    private Integer estimatedWaitTime;

    private LocalDateTime calledTime;

    private LocalDateTime expireTime;

    private LocalDateTime createTime;
}
