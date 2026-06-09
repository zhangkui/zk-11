package com.charging.vo;

import com.charging.entity.ChargingPile;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StationDetailVO {

    private Long id;

    private String name;

    private String address;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private Integer totalPiles;

    private Integer availablePiles;

    private Integer status;

    private String statusName;

    private String openingHours;

    private String phone;

    private String description;

    private Integer queueCount;

    private LocalDateTime createTime;

    private List<ChargingPile> piles;
}
