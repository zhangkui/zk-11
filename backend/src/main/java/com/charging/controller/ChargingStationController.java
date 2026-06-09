package com.charging.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.common.Result;
import com.charging.dto.StationQueryDTO;
import com.charging.dto.StationSaveDTO;
import com.charging.entity.ChargingStation;
import com.charging.service.ChargingStationService;
import com.charging.vo.StationDetailVO;
import com.charging.vo.TimeSlotStatusVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 充电站管理控制器
 */
@RestController
@RequestMapping("/station")
public class ChargingStationController {

    @Resource
    private ChargingStationService chargingStationService;

    @GetMapping("/page")
    public Result<Page<ChargingStation>> page(StationQueryDTO queryDTO) {
        return Result.success(chargingStationService.page(queryDTO));
    }

    @GetMapping("/{id}")
    public Result<StationDetailVO> getDetail(@PathVariable Long id) {
        return Result.success(chargingStationService.getDetail(id));
    }

    @PostMapping("/save")
    public Result<Void> save(@RequestBody @Valid StationSaveDTO saveDTO) {
        chargingStationService.save(saveDTO);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        chargingStationService.updateStatus(id, status);
        return Result.success();
    }

    @GetMapping("/{id}/time-slots")
    public Result<List<TimeSlotStatusVO>> getTimeSlotStatus(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(chargingStationService.getTimeSlotStatus(id, date));
    }
}
