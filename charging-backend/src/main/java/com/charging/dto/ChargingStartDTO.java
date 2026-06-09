package com.charging.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ChargingStartDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "站点ID不能为空")
    private Long stationId;

    @NotNull(message = "充电桩ID不能为空")
    private Long pileId;

    private Long reservationId;

    private Long queueId;

    private BigDecimal startSoc;
}
