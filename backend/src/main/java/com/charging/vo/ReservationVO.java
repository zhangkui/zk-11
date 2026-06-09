package com.charging.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约VO
 */
@Data
public class ReservationVO implements Serializable {

    private Long id;

    private Long userId;

    private String userName;

    private Long stationId;

    private String stationName;

    private Long pileId;

    private String pileNumber;

    private LocalDate reservationDate;

    private Integer timeSlot;

    private String timeSlotDesc;

    private String carPlate;

    private Integer status;

    private String statusDesc;

    private Integer queueNumber;

    private LocalDateTime expireTime;

    private LocalDateTime checkInTime;

    private LocalDateTime createTime;
}
