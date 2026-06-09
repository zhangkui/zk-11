package com.charging.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 充电桩实体类
 */
@Data
@TableName("charging_pile")
public class ChargingPile implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long stationId;

    private String pileNumber;

    private Integer type;

    private Integer power;

    private Integer status;

    private Long currentOrderId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
