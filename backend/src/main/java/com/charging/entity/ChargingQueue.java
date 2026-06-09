package com.charging.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 排队实体类
 */
@Data
@TableName("charging_queue")
public class ChargingQueue implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long stationId;

    private Long userId;

    private String carPlate;

    private Integer queueNumber;

    private Integer status;

    private LocalDateTime calledTime;

    private LocalDateTime expireTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
