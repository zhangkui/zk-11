package com.charging.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardStatsVO {

    private Long totalStations;

    private Long totalPiles;

    private Long todayQueue;

    private BigDecimal todayIncome;

    private List<PileStatusStatsVO> pileStatusStats;

    private List<TrendStatsVO> weekTrend;

    private List<StationStatsVO> stationStats;
}
