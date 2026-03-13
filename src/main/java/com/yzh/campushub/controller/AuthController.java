package com.yzh.campushub.controller;


import com.yzh.campushub.dto.LoginDTO;
import com.yzh.campushub.dto.RegisterDTO;
import com.yzh.campushub.dto.Result;
import com.yzh.campushub.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

//    用户注册
    @PostMapping("/register")
    public ResponseEntity<Result> register(@RequestBody RegisterDTO registerDTO){
        Result result = authService.register(registerDTO);
        return ResponseEntity.status(result.getCode()).body(result);
    }

//    用户登录
    @PostMapping("/login")
    public ResponseEntity<Result> login(@RequestBody LoginDTO loginDTO){
        Result result = authService.login(loginDTO);
        return ResponseEntity.status(result.getCode()).body(result);
    }

    //    获取当前登录用户信息
    @GetMapping("/me")
    public ResponseEntity<Result> getMe(){
        Result result = authService.getMe();
        return ResponseEntity.status(result.getCode()).body(result);
    }

}

