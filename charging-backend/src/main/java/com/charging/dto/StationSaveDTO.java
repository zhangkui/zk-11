package com.charging.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StationSaveDTO {

    private Long id;

    @NotBlank(message = "站点名称不能为空")
    private String name;

    @NotBlank(message = "站点地址不能为空")
    private String address;

    private BigDecimal longitude;

    private BigDecimal latitude;

    @NotNull(message = "充电桩总数不能为空")
    private Integer totalPiles;

    private Integer status;

    private String openingHours;

    private String phone;

    private String description;
}
