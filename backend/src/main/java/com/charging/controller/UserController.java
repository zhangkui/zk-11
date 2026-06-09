package com.charging.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.charging.common.Result;
import com.charging.entity.UserInfo;
import com.charging.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/{id}")
    public Result<UserInfo> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @GetMapping("/page")
    public Result<Page<UserInfo>> page(
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(userService.page(phone, status, pageNum, pageSize));
    }

    @PostMapping("/save")
    public Result<Void> save(@RequestBody UserInfo user) {
        userService.save(user);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.success();
    }

    @PostMapping("/{id}/recharge")
    public Result<Void> recharge(@PathVariable Long id, @RequestParam BigDecimal amount) {
        userService.recharge(id, amount);
        return Result.success();
    }
}
