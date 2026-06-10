package com.charging.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.charging.common.Constants;
import com.charging.dto.LoginDTO;
import com.charging.dto.RegisterDTO;
import com.charging.dto.UserUpdateDTO;
import com.charging.entity.User;
import com.charging.exception.BusinessException;
import com.charging.mapper.UserMapper;
import com.charging.service.UserService;
import com.charging.vo.UserInfoVO;
import cn.hutool.crypto.digest.MD5;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String REDIS_USER_TOKEN_KEY = "charging:user:token:";

    @Override
    public UserInfoVO login(LoginDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, dto.getPhone());
        wrapper.eq(User::getDeleted, 0);
        User user = this.getOne(wrapper);

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }

        String encryptPassword = MD5.create().digestHex(dto.getPassword());
        if (!encryptPassword.equals(user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        String token = java.util.UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(REDIS_USER_TOKEN_KEY + token, user.getId().toString(), 24, TimeUnit.HOURS);

        UserInfoVO vo = new UserInfoVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    @Override
    public void register(RegisterDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, dto.getPhone());
        wrapper.eq(User::getDeleted, 0);
        Long count = this.baseMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("该手机号已注册");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPhone(dto.getPhone());
        user.setPassword(MD5.create().digestHex(dto.getPassword()));
        user.setLicensePlate(dto.getLicensePlate());
        user.setStatus(1);
        this.save(user);
    }

    @Override
    public UserInfoVO getInfo(Long id) {
        User user = this.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        UserInfoVO vo = new UserInfoVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    @Override
    public void updateInfo(Long id, UserUpdateDTO dto) {
        User user = this.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (dto.getUsername() != null) {
            user.setUsername(dto.getUsername());
        }
        if (dto.getPhone() != null && !dto.getPhone().equals(user.getPhone())) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getPhone, dto.getPhone());
            wrapper.eq(User::getDeleted, 0);
            wrapper.ne(User::getId, id);
            Long count = this.baseMapper.selectCount(wrapper);
            if (count > 0) {
                throw new BusinessException("该手机号已被使用");
            }
            user.setPhone(dto.getPhone());
        }
        if (dto.getPassword() != null) {
            user.setPassword(MD5.create().digestHex(dto.getPassword()));
        }
        if (dto.getLicensePlate() != null) {
            user.setLicensePlate(dto.getLicensePlate());
        }

        this.updateById(user);
    }

    @Override
    public void logout(Long id) {
    }
}
