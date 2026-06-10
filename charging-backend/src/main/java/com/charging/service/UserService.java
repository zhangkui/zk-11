package com.charging.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.charging.dto.LoginDTO;
import com.charging.dto.RegisterDTO;
import com.charging.dto.UserUpdateDTO;
import com.charging.entity.User;
import com.charging.vo.UserInfoVO;

public interface UserService extends IService<User> {

    UserInfoVO login(LoginDTO dto);

    void register(RegisterDTO dto);

    UserInfoVO getInfo(Long id);

    void updateInfo(Long id, UserUpdateDTO dto);

    void logout(Long id);

    boolean removeById(Long id);
}
