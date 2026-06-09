package com.charging.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReservationCancelDTO {

    @NotNull(message = "预约ID不能为空")
    private Long id;

    private String cancelReason;
}
