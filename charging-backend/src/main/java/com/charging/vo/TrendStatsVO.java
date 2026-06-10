package com.charging.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TrendStatsVO {

    private String date;

    private BigDecimal totalKwh;

    private BigDecimal totalIncome;
}
