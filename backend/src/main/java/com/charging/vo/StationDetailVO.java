package com.charging.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 充电站详情VO
 */
@Data
public class StationDetailVO implements Serializable {

    private Long id;

    private String name;

    private String address;

    private Integer totalPiles;

    private Integer availablePiles;

    private BigDecimal pricePerKwh;

    private BigDecimal serviceFee;

    private Integer status;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private List<PileVO> piles;

    private LocalDateTime createTime;

    @Data
    public static class PileVO implements Serializable {
        private Long id;
        private String pileNumber;
        private Integer type;
        private Integer power;
        private Integer status;
    }
}
