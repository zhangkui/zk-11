package com.charging.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StationStatsVO {

    private Long id;

    private String name;

    private Integer totalPiles;

    private Integer availablePiles;

    private BigDecimal todayKwh;

    private BigDecimal todayIncome;
}
