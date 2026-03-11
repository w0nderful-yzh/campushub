package com.yzh.campushub.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzh.campushub.entity.User;
import com.yzh.campushub.mapper.UserMapper;
import com.yzh.campushub.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}
