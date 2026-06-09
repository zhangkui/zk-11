package com.charging.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.charging.common.Result;
import com.charging.dto.QueueJoinDTO;
import com.charging.service.ChargingQueueService;
import com.charging.vo.QueueInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "排队叫号管理")
@RestController
@RequestMapping("/queue")
public class ChargingQueueController {

    @Resource
    private ChargingQueueService queueService;

    @Operation(summary = "加入排队")
    @PostMapping("/join")
    public Result<QueueInfoVO> joinQueue(@Valid @RequestBody QueueJoinDTO dto) {
        return Result.success(queueService.joinQueue(dto));
    }

    @Operation(summary = "取消排队")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancelQueue(@PathVariable Long id) {
        queueService.cancelQueue(id);
        return Result.success();
    }

    @Operation(summary = "叫号")
    @PostMapping("/call/{stationId}")
    public Result<QueueInfoVO> callNext(@PathVariable Long stationId) {
        return Result.success(queueService.callNext(stationId));
    }

    @Operation(summary = "获取排队信息")
    @GetMapping("/{id}")
    public Result<QueueInfoVO> getQueueInfo(@PathVariable Long id) {
        return Result.success(queueService.getQueueInfo(id));
    }

    @Operation(summary = "获取用户排队列表")
    @GetMapping("/user/{userId}")
    public Result<IPage<QueueInfoVO>> pageByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(queueService.pageByUser(userId, pageNum, pageSize));
    }

    @Operation(summary = "获取前面排队人数")
    @GetMapping("/ahead/{stationId}/{queueNumber}")
    public Result<Integer> getAheadCount(@PathVariable Long stationId, @PathVariable Integer queueNumber) {
        return Result.success(queueService.getAheadCount(stationId, queueNumber));
    }
}
