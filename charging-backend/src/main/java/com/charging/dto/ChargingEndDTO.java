package com.charging.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ChargingEndDTO {

    @NotNull(message = "充电记录ID不能为空")
    private Long recordId;

    private BigDecimal endSoc;

    @NotNull(message = "充电量不能为空")
    private BigDecimal chargedKwh;

    private String stopReason;

    private Integer status;
}
