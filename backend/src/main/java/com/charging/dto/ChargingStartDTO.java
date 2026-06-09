package com.charging.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 开始充电DTO
 */
@Data
public class ChargingStartDTO implements Serializable {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "充电桩ID不能为空")
    private Long pileId;

    private Long reservationId;

    private Long queueId;

    @NotNull(message = "起始电量不能为空")
    private BigDecimal startKwh;
}
