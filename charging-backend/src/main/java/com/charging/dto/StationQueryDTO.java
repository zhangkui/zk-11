package com.charging.dto;

import lombok.Data;

@Data
public class StationQueryDTO {

    private String name;

    private Integer status;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
