package com.charging.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.common.Result;
import com.charging.dto.PaymentDTO;
import com.charging.entity.PaymentRecord;
import com.charging.service.PaymentService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 支付管理控制器
 */
@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Resource
    private PaymentService paymentService;

    @PostMapping("/pay")
    public Result<PaymentRecord> pay(@RequestBody @Valid PaymentDTO dto) {
        return Result.success(paymentService.pay(dto));
    }

    @GetMapping("/{id}")
    public Result<PaymentRecord> getDetail(@PathVariable Long id) {
        return Result.success(paymentService.getDetail(id));
    }

    @GetMapping("/page")
    public Result<Page<PaymentRecord>> page(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(paymentService.page(userId, status, pageNum, pageSize));
    }

    @PostMapping("/{id}/refund")
    public Result<Void> refund(@PathVariable Long id, @RequestParam(required = false) String reason) {
        paymentService.refund(id, reason);
        return Result.success();
    }
}
