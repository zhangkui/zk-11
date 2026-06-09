package com.charging.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.common.Result;
import com.charging.dto.QueueJoinDTO;
import com.charging.entity.ChargingQueue;
import com.charging.service.ChargingQueueService;
import com.charging.vo.QueueStatusVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 排队叫号控制器
 */
@RestController
@RequestMapping("/queue")
public class ChargingQueueController {

    @Resource
    private ChargingQueueService chargingQueueService;

    @PostMapping("/join")
    public Result<ChargingQueue> join(@RequestBody @Valid QueueJoinDTO dto) {
        return Result.success(chargingQueueService.join(dto));
    }

    @GetMapping("/{id}")
    public Result<QueueStatusVO> getStatus(@PathVariable Long id) {
        return Result.success(chargingQueueService.getStatus(id));
    }

    @GetMapping("/page")
    public Result<Page<QueueStatusVO>> page(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long stationId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(chargingQueueService.page(userId, stationId, status, pageNum, pageSize));
    }

    @PostMapping("/{stationId}/call-next")
    public Result<Void> callNext(@PathVariable Long stationId) {
        chargingQueueService.callNext(stationId);
        return Result.success();
    }

    @PutMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id) {
        chargingQueueService.complete(id);
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        chargingQueueService.cancel(id);
        return Result.success();
    }
}
