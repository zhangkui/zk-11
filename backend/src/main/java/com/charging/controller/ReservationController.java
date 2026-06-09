package com.charging.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.common.Result;
import com.charging.dto.ReservationCreateDTO;
import com.charging.entity.Reservation;
import com.charging.service.ReservationService;
import com.charging.vo.ReservationVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 预约管理控制器
 */
@RestController
@RequestMapping("/reservation")
public class ReservationController {

    @Resource
    private ReservationService reservationService;

    @PostMapping("/create")
    public Result<Reservation> create(@RequestBody @Valid ReservationCreateDTO dto) {
        return Result.success(reservationService.create(dto));
    }

    @GetMapping("/{id}")
    public Result<ReservationVO> getDetail(@PathVariable Long id) {
        return Result.success(reservationService.getDetail(id));
    }

    @GetMapping("/page")
    public Result<Page<ReservationVO>> page(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long stationId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(reservationService.page(userId, stationId, status, pageNum, pageSize));
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        reservationService.cancel(id);
        return Result.success();
    }

    @PutMapping("/{id}/check-in")
    public Result<Void> checkIn(@PathVariable Long id) {
        reservationService.checkIn(id);
        return Result.success();
    }

    @PutMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id) {
        reservationService.complete(id);
        return Result.success();
    }
}
