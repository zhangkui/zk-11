package com.charging.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.charging.common.Constants;
import com.charging.common.Result;
import com.charging.common.UserContext;
import com.charging.dto.QueueJoinDTO;
import com.charging.entity.ChargingQueue;
import com.charging.service.ChargingQueueService;
import com.charging.vo.QueueInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
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
    public Result<QueueInfoVO> joinQueue(@Valid @RequestBody QueueJoinDTO dto, HttpServletRequest request) {
        UserContext.validateUserRole(Constants.UserRole.USER);
        Long currentUserId = (Long) request.getAttribute(Constants.REQUEST_ATTR_USER_ID);
        dto.setUserId(currentUserId);
        return Result.success(queueService.joinQueue(dto));
    }

    @Operation(summary = "取消排队")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancelQueue(@PathVariable Long id) {
        UserContext.validateUserRole(Constants.UserRole.USER);
        QueueInfoVO queueInfo = queueService.getQueueInfo(id);
        if (queueInfo != null) {
            ChargingQueue queue = queueService.getById(id);
            if (queue != null) {
                UserContext.validateUserId(queue.getUserId());
            }
        }
        queueService.cancelQueue(id);
        return Result.success();
    }

    @Operation(summary = "叫号")
    @PostMapping("/call/{stationId}")
    public Result<QueueInfoVO> callNext(@PathVariable Long stationId) {
        UserContext.validateUserRole(Constants.UserRole.ADMIN);
        return Result.success(queueService.callNext(stationId));
    }

    @Operation(summary = "获取排队信息")
    @GetMapping("/{id}")
    public Result<QueueInfoVO> getQueueInfo(@PathVariable Long id) {
        QueueInfoVO queueInfo = queueService.getQueueInfo(id);
        if (queueInfo != null && UserContext.isUser()) {
            ChargingQueue queue = queueService.getById(id);
            if (queue != null) {
                UserContext.validateUserId(queue.getUserId());
            }
        }
        return Result.success(queueInfo);
    }

    @Operation(summary = "获取用户排队列表")
    @GetMapping("/user/{userId}")
    public Result<IPage<QueueInfoVO>> pageByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        if (UserContext.isUser()) {
            UserContext.validateUserId(userId);
        }
        return Result.success(queueService.pageByUser(userId, pageNum, pageSize));
    }

    @Operation(summary = "获取前面排队人数")
    @GetMapping("/ahead/{stationId}/{queueNumber}")
    public Result<Integer> getAheadCount(@PathVariable Long stationId, @PathVariable Integer queueNumber) {
        return Result.success(queueService.getAheadCount(stationId, queueNumber));
    }
}
