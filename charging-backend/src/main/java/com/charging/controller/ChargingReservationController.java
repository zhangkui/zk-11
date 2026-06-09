package com.charging.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.charging.common.Result;
import com.charging.dto.ReservationCancelDTO;
import com.charging.dto.ReservationCreateDTO;
import com.charging.entity.ChargingReservation;
import com.charging.service.ChargingReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "预约管理")
@RestController
@RequestMapping("/reservation")
public class ChargingReservationController {

    @Resource
    private ChargingReservationService reservationService;

    @Operation(summary = "创建预约")
    @PostMapping("/create")
    public Result<ChargingReservation> create(@Valid @RequestBody ReservationCreateDTO dto) {
        return Result.success(reservationService.create(dto));
    }

    @Operation(summary = "取消预约")
    @PostMapping("/cancel")
    public Result<Void> cancel(@Valid @RequestBody ReservationCancelDTO dto) {
        reservationService.cancel(dto);
        return Result.success();
    }

    @Operation(summary = "确认到店")
    @PutMapping("/{id}/arrive")
    public Result<Void> confirmArrive(@PathVariable Long id) {
        reservationService.confirmArrive(id);
        return Result.success();
    }

    @Operation(summary = "获取用户预约列表")
    @GetMapping("/user/{userId}")
    public Result<IPage<ChargingReservation>> pageByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(reservationService.pageByUser(userId, pageNum, pageSize));
    }

    @Operation(summary = "获取预约详情")
    @GetMapping("/{id}")
    public Result<ChargingReservation> getDetail(@PathVariable Long id) {
        return Result.success(reservationService.getById(id));
    }
}
