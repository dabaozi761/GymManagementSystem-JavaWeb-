package com.dabaozi.gymmanagementsystem.service.impl;

import com.dabaozi.gymmanagementsystem.common.convention.result.Result;
import com.dabaozi.gymmanagementsystem.pojo.dto.LoginDTO;
import com.dabaozi.gymmanagementsystem.pojo.entity.Admin;
import com.dabaozi.gymmanagementsystem.mapper.AdminMapper;
import com.dabaozi.gymmanagementsystem.mapper.MemberMapper;
import com.dabaozi.gymmanagementsystem.mapper.UserMapper;
import com.dabaozi.gymmanagementsystem.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class LoginServiceImpl implements LoginService {

    private final AdminMapper adminMapper;

    private final UserMapper userMapper;

    private final MemberMapper memberMapper;
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
