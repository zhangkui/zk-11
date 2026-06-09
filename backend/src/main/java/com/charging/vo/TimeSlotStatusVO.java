package com.charging.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 时段状态VO
 */
@Data
public class TimeSlotStatusVO implements Serializable {

    private Integer timeSlot;

    private String timeSlotDesc;

    private Integer status;

    private String statusDesc;

    private Integer availableCount;

    private Integer totalCount;
}
