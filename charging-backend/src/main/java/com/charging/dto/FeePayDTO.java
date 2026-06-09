package com.charging.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FeePayDTO {

    @NotNull(message = "费用ID不能为空")
    private Long feeId;

    @NotNull(message = "支付方式不能为空")
    private Integer payMethod;
}
