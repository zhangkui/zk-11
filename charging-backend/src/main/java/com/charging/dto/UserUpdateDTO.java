package com.charging.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDTO {

    private String username;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    private String password;

    private String licensePlate;
}
