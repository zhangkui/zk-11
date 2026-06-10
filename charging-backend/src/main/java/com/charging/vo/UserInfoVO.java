package com.charging.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserInfoVO {

    private Long id;

    private String username;

    private String phone;

    private String licensePlate;

    private BigDecimal balance;

    private Integer status;

    private LocalDateTime createTime;
}
