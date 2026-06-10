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
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @PostConstruct
    public void initDefaultAdmin() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getIsDefault, 1);
        wrapper.eq(User::getRole, Constants.UserRole.ADMIN);
        wrapper.eq(User::getDeleted, 0);
        Long count = this.baseMapper.selectCount(wrapper);
        if (count == 0) {
            User admin = new User();
            admin.setUsername("系统管理员");
            admin.setPhone("admin");
            admin.setPassword(MD5.create().digestHex("123456"));
            admin.setBalance(BigDecimal.ZERO);
            admin.setRole(Constants.UserRole.ADMIN);
            admin.setStatus(1);
            admin.setIsDefault(1);
            this.save(admin);
        }
    }

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
        String tokenValue = user.getId() + ":" + user.getRole();
        stringRedisTemplate.opsForValue().set(Constants.REDIS_USER_TOKEN_KEY + token, tokenValue, 24, TimeUnit.HOURS);

        UserInfoVO vo = new UserInfoVO();
        BeanUtils.copyProperties(user, vo);
        vo.setToken(token);
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
        user.setRole(Constants.UserRole.USER);
        user.setStatus(1);
        user.setIsDefault(0);
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

        if (user.getIsDefault() == 1) {
            throw new BusinessException("默认管理员账号不可修改");
        }

        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<User> updateWrapper =
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, id);

        if (dto.getUsername() != null) {
            updateWrapper.set(User::getUsername, dto.getUsername());
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
            updateWrapper.set(User::getPhone, dto.getPhone());
        }
        if (dto.getPassword() != null) {
            updateWrapper.set(User::getPassword, MD5.create().digestHex(dto.getPassword()));
        }
        if (dto.getLicensePlate() != null) {
            updateWrapper.set(User::getLicensePlate, dto.getLicensePlate());
        }

        this.baseMapper.update(null, updateWrapper);
    }

    @Override
    public boolean removeById(Long id) {
        User user = this.getById(id);
        if (user != null && user.getIsDefault() == 1) {
            throw new BusinessException("默认管理员账号不可删除");
        }
        return super.removeById(id);
    }

    @Override
    public void logout(Long id) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader(Constants.REQUEST_HEADER_TOKEN);
        if (token != null) {
            stringRedisTemplate.delete(Constants.REDIS_USER_TOKEN_KEY + token);
        }
    }
}
