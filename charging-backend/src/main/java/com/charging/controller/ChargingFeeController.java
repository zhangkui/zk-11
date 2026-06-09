package com.charging.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.charging.common.Result;
import com.charging.dto.FeePayDTO;
import com.charging.entity.ChargingFee;
import com.charging.service.ChargingFeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "费用管理")
@RestController
@RequestMapping("/fee")
public class ChargingFeeController {

    @Resource
    private ChargingFeeService feeService;

    @Operation(summary = "支付费用")
    @PostMapping("/pay")
    public Result<ChargingFee> pay(@Valid @RequestBody FeePayDTO dto) {
        return Result.success(feeService.pay(dto));
    }

    @Operation(summary = "获取用户费用列表")
    @GetMapping("/user/{userId}")
    public Result<IPage<ChargingFee>> pageByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(feeService.pageByUser(userId, pageNum, pageSize));
    }

    @Operation(summary = "根据充电记录获取费用")
    @GetMapping("/record/{recordId}")
    public Result<ChargingFee> getByRecordId(@PathVariable Long recordId) {
        return Result.success(feeService.getByRecordId(recordId));
    }

    @Operation(summary = "获取费用详情")
    @GetMapping("/{id}")
    public Result<ChargingFee> getDetail(@PathVariable Long id) {
        return Result.success(feeService.getById(id));
    }
}
