package com.charging.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.charging.common.Result;
import com.charging.dto.StationQueryDTO;
import com.charging.dto.StationSaveDTO;
import com.charging.entity.ChargingStation;
import com.charging.service.ChargingStationService;
import com.charging.vo.StationDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "充电站点管理")
@RestController
@RequestMapping("/station")
public class ChargingStationController {

    @Resource
    private ChargingStationService chargingStationService;

    @Operation(summary = "分页查询站点列表")
    @GetMapping("/page")
    public Result<IPage<ChargingStation>> page(StationQueryDTO dto) {
        return Result.success(chargingStationService.pageList(dto));
    }

    @Operation(summary = "获取站点详情")
    @GetMapping("/{id}")
    public Result<StationDetailVO> getDetail(@PathVariable Long id) {
        return Result.success(chargingStationService.getDetail(id));
    }

    @Operation(summary = "新增/编辑站点")
    @PostMapping("/save")
    public Result<Void> save(@Valid @RequestBody StationSaveDTO dto) {
        chargingStationService.saveStation(dto);
        return Result.success();
    }

    @Operation(summary = "更新站点状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        chargingStationService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "删除站点")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        chargingStationService.removeById(id);
        return Result.success();
    }
}
