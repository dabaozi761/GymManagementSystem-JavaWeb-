package com.dabaozi.gymmanagementsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dabaozi.gymmanagementsystem.pojo.entity.User;
import com.dabaozi.gymmanagementsystem.mapper.UserMapper;
import com.dabaozi.gymmanagementsystem.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
