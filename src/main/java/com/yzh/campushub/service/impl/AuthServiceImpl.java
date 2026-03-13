package com.yzh.campushub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yzh.campushub.dto.LoginDTO;
import com.yzh.campushub.dto.RegisterDTO;
import com.yzh.campushub.dto.Result;
import com.yzh.campushub.entity.User;
import com.yzh.campushub.mapper.UserMapper;
import com.yzh.campushub.service.AuthService;
import com.yzh.campushub.service.UserService;
import com.yzh.campushub.utils.Constants;
import com.yzh.campushub.utils.JwtUtil;
import com.yzh.campushub.vo.LoginVO;
import com.yzh.campushub.vo.UserInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import com.yzh.campushub.utils.UserContext;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public Result register(RegisterDTO registerDTO) {
        if(registerDTO.getUsername()==null||registerDTO.getNickname()==null||registerDTO.getPassword()==null)
            return Result.fail(Constants.CODE_400, Constants.MSG_PARAM_ERROR);
        if(!registerDTO.getPassword().equals(registerDTO.getConfirmPassword()))
            return Result.fail(Constants.CODE_400, Constants.MSG_PWD_DIFF);
//        帐号不得重复
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername,registerDTO.getUsername());
        if(userMapper.selectCount(wrapper)>0){
            return Result.fail(Constants.CODE_400, Constants.MSG_USER_EXIST);
        }
//        保存到数据库
        User user = new User()
                .setUsername(registerDTO.getUsername())
                .setPassword(passwordEncoder.encode(registerDTO.getPassword()))
                .setNickname(registerDTO.getNickname())
                .setAvatar("F:\\MyWork\\images\\default_avatar.jpg")
                .setStatus(1)
                .setCreateTime(LocalDateTime.now());
        userService.save(user);
        log.info("注册成功");
        return Result.ok(Constants.MSG_REGISTER_SUCCESS);
    }

    @Override
    public Result login(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        if(username==null || password==null)
            return Result.fail(Constants.CODE_400, Constants.MSG_PARAM_ERROR);
        
        // 1. 根据用户名查询用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
        );
        
        // 2. 校验用户是否存在及状态
        if(user==null){
            return Result.fail(Constants.CODE_404, Constants.MSG_USER_NOT_FOUND);
        }
        if(user.getStatus()==0)
            return Result.fail(Constants.CODE_403, Constants.MSG_ACCOUNT_DISABLED);
            
        // 3. 校验密码
        if(!passwordEncoder.matches(password, user.getPassword()))
            return Result.fail(Constants.CODE_401, Constants.MSG_LOGIN_FAILED);
            
        // 4. 生成JWT令牌
        Long userId = user.getId();
        String token = JwtUtil.generateToken(userId,username);
        
        // 5. 将token存入Redis，设置过期时间（与JWT过期时间一致，7天）
        stringRedisTemplate.opsForValue().set("login:token:" + userId, token, 7, TimeUnit.DAYS);
        
        // 6. 构建返回VO
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(user, userInfoVO);
        // 处理role字段类型转换 (User中是Integer，UserInfoVO中是String)
        if(user.getRole() != null) {
            userInfoVO.setRole(String.valueOf(user.getRole()));
        }
        
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserInfo(userInfoVO);
        
        return Result.ok(loginVO);
    }

    @Override
    public Result getMe() {
        // 1. 从ThreadLocal获取当前登录用户ID
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.fail(Constants.CODE_401, Constants.MSG_LOGIN_FAILED);
        }

        // 2. 查询数据库获取最新用户信息
        User user = userService.getById(userId);
        if (user == null) {
            return Result.fail(Constants.CODE_404, Constants.MSG_USER_NOT_FOUND);
        }

        // 3. 构建返回VO
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(user, userInfoVO);
        if (user.getRole() != null) {
            userInfoVO.setRole(String.valueOf(user.getRole()));
        }

        return Result.ok(userInfoVO);
    }
}

