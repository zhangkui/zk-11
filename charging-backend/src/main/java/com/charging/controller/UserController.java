package com.charging.controller;

import com.charging.common.Constants;
import com.charging.common.Result;
import com.charging.common.UserContext;
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
import jakarta.servlet.http.HttpServletRequest;
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
    @GetMapping("/info")
    public Result<UserInfoVO> getInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(Constants.REQUEST_ATTR_USER_ID);
        return Result.success(userService.getInfo(userId));
    }

    @Operation(summary = "更新当前用户信息")
    @PutMapping("/info")
    public Result<Void> updateInfo(HttpServletRequest request, @Valid @RequestBody UserUpdateDTO dto) {
        Long userId = (Long) request.getAttribute(Constants.REQUEST_ATTR_USER_ID);
        UserContext.validateUserId(userId);
        userService.updateInfo(userId, dto);
        return Result.success();
    }

    @Operation(summary = "用户退出登录")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(Constants.REQUEST_ATTR_USER_ID);
        userService.logout(userId);
        return Result.success();
    }

    @Operation(summary = "获取所有用户（管理员）")
    @GetMapping("/list")
    public Result<List<User>> list() {
        UserContext.validateUserRole(Constants.UserRole.ADMIN);
        return Result.success(userMapper.selectList(null));
    }

    @Operation(summary = "获取用户详情（管理员）")
    @GetMapping("/{id}")
    public Result<User> getDetail(@PathVariable Long id) {
        UserContext.validateUserRole(Constants.UserRole.ADMIN);
        return Result.success(userMapper.selectById(id));
    }

    @Operation(summary = "删除用户（管理员）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        UserContext.validateUserRole(Constants.UserRole.ADMIN);
        userService.removeById(id);
        return Result.success();
    }
}
