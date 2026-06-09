package com.charging.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QueueJoinDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "站点ID不能为空")
    private Long stationId;

    private Integer pileType;

    private String remark;
}
