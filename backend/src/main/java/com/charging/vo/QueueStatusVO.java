package com.charging.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 排队状态VO
 */
@Data
public class QueueStatusVO implements Serializable {

    private Long id;

    private Long stationId;

    private String stationName;

    private Long userId;

    private String carPlate;

    private Integer queueNumber;

    private Integer status;

    private String statusDesc;

    private Integer aheadCount;

    private LocalDateTime calledTime;

    private LocalDateTime expireTime;

    private LocalDateTime createTime;
}
