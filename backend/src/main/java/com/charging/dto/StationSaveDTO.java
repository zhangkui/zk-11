package com.charging.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 充电站保存DTO
 */
@Data
public class StationSaveDTO implements Serializable {

    private Long id;

    @NotBlank(message = "站点名称不能为空")
    private String name;

    @NotBlank(message = "站点地址不能为空")
    private String address;

    @NotNull(message = "充电桩总数不能为空")
    private Integer totalPiles;

    private BigDecimal pricePerKwh;

    private BigDecimal serviceFee;

    private Integer status;

    private BigDecimal longitude;

    private BigDecimal latitude;
}
