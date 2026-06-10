package com.charging.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.charging.common.Constants;
import com.charging.common.Result;
import com.charging.common.UserContext;
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
        UserContext.validateUserRole(Constants.UserRole.USER);
        ChargingFee fee = feeService.getById(dto.getFeeId());
        if (fee != null) {
            UserContext.validateUserId(fee.getUserId());
        }
        return Result.success(feeService.pay(dto));
    }

    @Operation(summary = "获取用户费用列表")
    @GetMapping("/user/{userId}")
    public Result<IPage<ChargingFee>> pageByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        if (UserContext.isUser()) {
            UserContext.validateUserId(userId);
        }
        return Result.success(feeService.pageByUser(userId, pageNum, pageSize));
    }

    @Operation(summary = "根据充电记录获取费用")
    @GetMapping("/record/{recordId}")
    public Result<ChargingFee> getByRecordId(@PathVariable Long recordId) {
        ChargingFee fee = feeService.getByRecordId(recordId);
        if (fee != null && UserContext.isUser()) {
            UserContext.validateUserId(fee.getUserId());
        }
        return Result.success(fee);
    }

    @Operation(summary = "获取费用详情")
    @GetMapping("/{id}")
    public Result<ChargingFee> getDetail(@PathVariable Long id) {
        ChargingFee fee = feeService.getById(id);
        if (fee != null && UserContext.isUser()) {
            UserContext.validateUserId(fee.getUserId());
        }
        return Result.success(fee);
    }
}
