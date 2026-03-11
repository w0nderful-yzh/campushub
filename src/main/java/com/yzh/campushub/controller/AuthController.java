package com.yzh.campushub.controller;


import com.yzh.campushub.dto.RegisterDTO;
import com.yzh.campushub.dto.Result;
import com.yzh.campushub.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

//    用户注册
    @PostMapping("/register")
    public Result register(@RequestBody RegisterDTO registerDTO){
        return authService.register(registerDTO);
    }

}

