package com.charging.controller;

import com.charging.common.Result;
import com.charging.dto.LoginDTO;
import com.charging.dto.RegisterDTO;
import com.charging.dto.UserUpdateDTO;
import com.charging.entity.User;
import com.charging.mapper.UserMapper;
import com.charging.service.UserService;
import com.charging.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserService userService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<UserInfoVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info/{id}")
    public Result<UserInfoVO> getInfo(@PathVariable Long id) {
        return Result.success(userService.getInfo(id));
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/{id}")
    public Result<Void> updateInfo(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) {
        userService.updateInfo(id, dto);
        return Result.success();
    }

    @Operation(summary = "用户退出登录")
    @PostMapping("/logout/{id}")
    public Result<Void> logout(@PathVariable Long id) {
        userService.logout(id);
        return Result.success();
    }

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
