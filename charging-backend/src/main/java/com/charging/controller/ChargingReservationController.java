package com.charging.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.charging.common.Constants;
import com.charging.common.Result;
import com.charging.common.UserContext;
import com.charging.dto.ReservationCancelDTO;
import com.charging.dto.ReservationCreateDTO;
import com.charging.entity.ChargingReservation;
import com.charging.service.ChargingReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
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
    public Result<ChargingReservation> create(@Valid @RequestBody ReservationCreateDTO dto, HttpServletRequest request) {
        UserContext.validateUserRole(Constants.UserRole.USER);
        Long currentUserId = (Long) request.getAttribute(Constants.REQUEST_ATTR_USER_ID);
        dto.setUserId(currentUserId);
        return Result.success(reservationService.create(dto));
    }

    @Operation(summary = "取消预约")
    @PostMapping("/cancel")
    public Result<Void> cancel(@Valid @RequestBody ReservationCancelDTO dto) {
        UserContext.validateUserRole(Constants.UserRole.USER);
        ChargingReservation reservation = reservationService.getById(dto.getId());
        if (reservation != null) {
            UserContext.validateUserId(reservation.getUserId());
        }
        reservationService.cancel(dto);
        return Result.success();
    }

    @Operation(summary = "确认到店")
    @PutMapping("/{id}/arrive")
    public Result<Void> confirmArrive(@PathVariable Long id) {
        UserContext.validateUserRole(Constants.UserRole.USER);
        ChargingReservation reservation = reservationService.getById(id);
        if (reservation != null) {
            UserContext.validateUserId(reservation.getUserId());
        }
        reservationService.confirmArrive(id);
        return Result.success();
    }

    @Operation(summary = "获取用户预约列表")
    @GetMapping("/user/{userId}")
    public Result<IPage<ChargingReservation>> pageByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        if (UserContext.isUser()) {
            UserContext.validateUserId(userId);
        }
        return Result.success(reservationService.pageByUser(userId, pageNum, pageSize));
    }

    @Operation(summary = "获取预约详情")
    @GetMapping("/{id}")
    public Result<ChargingReservation> getDetail(@PathVariable Long id) {
        ChargingReservation reservation = reservationService.getById(id);
        if (reservation != null && UserContext.isUser()) {
            UserContext.validateUserId(reservation.getUserId());
        }
        return Result.success(reservation);
    }
}
