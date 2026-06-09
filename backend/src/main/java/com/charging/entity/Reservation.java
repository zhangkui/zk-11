package com.charging.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约实体类
 */
@Data
@TableName("reservation")
public class Reservation implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long stationId;

    private Long pileId;

    private LocalDate reservationDate;

    private Integer timeSlot;

    private String carPlate;

    private Integer status;

    private Integer queueNumber;

    private LocalDateTime expireTime;

    private LocalDateTime checkInTime;

    private LocalDateTime cancelTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
