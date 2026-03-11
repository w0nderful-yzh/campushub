package com.yzh.campushub.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.yzh.campushub.dto.RegisterDTO;
import com.yzh.campushub.dto.Result;
import com.yzh.campushub.entity.User;

public interface AuthService{

//    注册
    Result register(RegisterDTO registerDTO);

}

