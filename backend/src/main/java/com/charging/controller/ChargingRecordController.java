package com.charging.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.common.Result;
import com.charging.dto.ChargingEndDTO;
import com.charging.dto.ChargingStartDTO;
import com.charging.entity.ChargingRecord;
import com.charging.service.ChargingRecordService;
import com.charging.vo.ChargingRecordVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 充电记录控制器
 */
@RestController
@RequestMapping("/charging")
public class ChargingRecordController {

    @Resource
    private ChargingRecordService chargingRecordService;

    @PostMapping("/start")
    public Result<ChargingRecord> start(@RequestBody @Valid ChargingStartDTO dto) {
        return Result.success(chargingRecordService.start(dto));
    }

    @PostMapping("/end")
    public Result<ChargingRecord> end(@RequestBody @Valid ChargingEndDTO dto) {
        return Result.success(chargingRecordService.end(dto));
    }

    @GetMapping("/{id}")
    public Result<ChargingRecordVO> getDetail(@PathVariable Long id) {
        return Result.success(chargingRecordService.getDetail(id));
    }

    @GetMapping("/page")
    public Result<Page<ChargingRecordVO>> page(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long stationId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer paymentStatus,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(chargingRecordService.page(userId, stationId, status, paymentStatus, pageNum, pageSize));
    }
}
