package com.charging.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.charging.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {

    private String username;

    private String phone;

    private String password;

    private String licensePlate;

    private BigDecimal balance;

    private Integer status;
}
