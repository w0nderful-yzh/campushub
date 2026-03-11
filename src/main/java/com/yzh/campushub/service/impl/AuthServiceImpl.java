package com.yzh.campushub.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yzh.campushub.dto.RegisterDTO;
import com.yzh.campushub.dto.Result;
import com.yzh.campushub.entity.User;
import com.yzh.campushub.mapper.UserMapper;
import com.yzh.campushub.service.AuthService;
import com.yzh.campushub.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserService userService;

    public AuthServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public Result register(RegisterDTO registerDTO) {
        if(registerDTO.getUsername()==null||registerDTO.getNickname()==null||registerDTO.getPassword()==null)
            return Result.fail("输入信息有误");
        if(!registerDTO.getPassword().equals(registerDTO.getConfirmPassword()))
            return Result.fail("两次密码不同");
//        帐号不得重复
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername,registerDTO.getUsername());
        if(userMapper.selectCount(wrapper)>0){
            return Result.fail("用户已存在");
        }
//        保存到数据库
        User user = new User()
                .setUsername(registerDTO.getUsername())
                .setPassword(registerDTO.getPassword())
                .setNickname(registerDTO.getNickname())
                .setAvatar("F:\\MyWork\\images\\_cgi-bin_mmwebwx-bin_webwxgetmsgimg__&MsgID=7941850319577588940&skey=@crypt_5e091767_aee6475ea5cad088751b98690d8420b2&mmweb_appid=wx_webfilehelper.jpg")
                .setStatus(1)
                .setCreateTime(LocalDateTime.now());
        userService.save(user);
        log.info("注册成功");
        return Result.ok("注册成功");
    }
}

