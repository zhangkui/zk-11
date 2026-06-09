package com.charging.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 排队加入DTO
 */
@Data
public class QueueJoinDTO implements Serializable {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "站点ID不能为空")
    private Long stationId;

    @NotBlank(message = "车牌号不能为空")
    private String carPlate;
}
