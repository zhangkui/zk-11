package com.charging.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.charging.common.Result;
import com.charging.dto.ChargingEndDTO;
import com.charging.dto.ChargingStartDTO;
import com.charging.entity.ChargingRecord;
import com.charging.service.ChargingRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "充电记录管理")
@RestController
@RequestMapping("/record")
public class ChargingRecordController {

    @Resource
    private ChargingRecordService recordService;

    @Operation(summary = "开始充电")
    @PostMapping("/start")
    public Result<ChargingRecord> startCharging(@Valid @RequestBody ChargingStartDTO dto) {
        return Result.success(recordService.startCharging(dto));
    }

    @Operation(summary = "结束充电")
    @PostMapping("/end")
    public Result<ChargingRecord> endCharging(@Valid @RequestBody ChargingEndDTO dto) {
        return Result.success(recordService.endCharging(dto));
    }

    @Operation(summary = "获取用户充电记录列表")
    @GetMapping("/user/{userId}")
    public Result<IPage<ChargingRecord>> pageByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(recordService.pageByUser(userId, pageNum, pageSize));
    }

    @Operation(summary = "获取当前充电订单")
    @GetMapping("/current")
    public Result<ChargingRecord> getCurrentCharging(
            @RequestParam Long userId,
            @RequestParam(required = false) Long stationId) {
        return Result.success(recordService.getCurrentCharging(userId, stationId));
    }

    @Operation(summary = "获取充电记录详情")
    @GetMapping("/{id}")
    public Result<ChargingRecord> getDetail(@PathVariable Long id) {
        return Result.success(recordService.getById(id));
    }
}
