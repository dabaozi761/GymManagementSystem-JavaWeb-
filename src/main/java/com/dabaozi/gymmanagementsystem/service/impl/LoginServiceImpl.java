package com.dabaozi.gymmanagementsystem.service.impl;

import com.dabaozi.gymmanagementsystem.common.convention.result.Result;
import com.dabaozi.gymmanagementsystem.dto.LoginDTO;
import com.dabaozi.gymmanagementsystem.mapper.AdminMapper;
import com.dabaozi.gymmanagementsystem.mapper.MemberMapper;
import com.dabaozi.gymmanagementsystem.mapper.UserMapper;
import com.dabaozi.gymmanagementsystem.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MemberMapper memberMapper;
    @Override
    public Result login(LoginDTO loginDTO) {
        String username=loginDTO.getUsername();
        String password=loginDTO.getPassword();
        Integer role=loginDTO.getRole();

        if(role==1){
            //管理员admin
            Admin admin= adminMapper.

        }
    }
}
