package com.charging.controller;

import com.charging.common.Result;
import com.charging.entity.User;
import com.charging.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserMapper userMapper;

    @Operation(summary = "获取所有用户")
    @GetMapping("/list")
    public Result<List<User>> list() {
        return Result.success(userMapper.selectList(null));
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    public Result<User> getDetail(@PathVariable Long id) {
        return Result.success(userMapper.selectById(id));
    }
}
