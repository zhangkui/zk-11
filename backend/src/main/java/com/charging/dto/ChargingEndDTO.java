package com.charging.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 结束充电DTO
 */
@Data
public class ChargingEndDTO implements Serializable {

    @NotNull(message = "充电记录ID不能为空")
    private Long recordId;

    @NotNull(message = "结束电量不能为空")
    private BigDecimal endKwh;
}
