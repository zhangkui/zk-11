package com.charging.controller;

import com.charging.common.Result;
import com.charging.service.DashboardService;
import com.charging.vo.DashboardStatsVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public Result<DashboardStatsVO> getStats() {
        return Result.success(dashboardService.getStats());
    }
}
