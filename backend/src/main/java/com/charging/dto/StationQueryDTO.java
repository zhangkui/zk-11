package com.charging.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 充电站查询DTO
 */
@Data
public class StationQueryDTO implements Serializable {

    private String name;

    private Integer status;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private Double radius;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
