package com.dabaozi.gymmanagementsystem.service.impl;

import com.dabaozi.gymmanagementsystem.common.convention.exception.AccountLockedException;
import com.dabaozi.gymmanagementsystem.common.convention.exception.AccountNotFoundException;
import com.dabaozi.gymmanagementsystem.common.convention.exception.PasswordErrorException;
import com.dabaozi.gymmanagementsystem.common.convention.result.Result;
import com.dabaozi.gymmanagementsystem.common.convention.result.Results;
import com.dabaozi.gymmanagementsystem.common.convention.utils.JwtUtils;
import com.dabaozi.gymmanagementsystem.pojo.dto.LoginDTO;
import com.dabaozi.gymmanagementsystem.pojo.entity.Admin;
import com.dabaozi.gymmanagementsystem.mapper.AdminMapper;
import com.dabaozi.gymmanagementsystem.mapper.MemberMapper;
import com.dabaozi.gymmanagementsystem.mapper.UserMapper;
import com.dabaozi.gymmanagementsystem.pojo.entity.Member;
import com.dabaozi.gymmanagementsystem.pojo.entity.User;
import com.dabaozi.gymmanagementsystem.pojo.vo.Loginresult;
import com.dabaozi.gymmanagementsystem.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.plugin.Interceptor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class LoginServiceImpl implements LoginService {

    private final AdminMapper adminMapper;

    private final UserMapper userMapper;

    private final MemberMapper memberMapper;

    private final JwtUtils jwtUtils;

    @Override
    public Result login(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        Integer role = loginDTO.getRole();

        Integer realRole=role;//判断是普通用户还是会员
        //用来封装令牌的自定义信息
        Long loginId;
        String name;


        if (role == 1) {
            //管理员admin
            Admin admin = adminMapper.selectByUsername(username);
            if (admin == null) {
                throw new AccountNotFoundException("账号没找到");
            }
            if (admin.getStatus() == 0) {
                throw new AccountLockedException("账号已经禁用");


            }
            //密码比对
            // TODO 后期需要进行md5加密，然后再进行比对
            // 将页面提交的密码进行md5加密
            password = DigestUtils.md5DigestAsHex(password.getBytes());
            if (!password.equals(admin.getPassword())) {
                //密码错误
                throw new PasswordErrorException("密码有问题");
            }
            loginId = admin.getId();
            name = admin.getName();

        } else if (role == 2) {

            User user = userMapper.selectByUsername(username);
            if (user == null) {
                throw new AccountNotFoundException("账号没找到");
            }
            if (user.getStatus() == 0) {
                throw new AccountLockedException("账号已经禁用");


            }
            //密码比对
            // TODO 后期需要进行md5加密，然后再进行比对
            // 将页面提交的密码进行md5加密
            password = DigestUtils.md5DigestAsHex(password.getBytes());
            if (!password.equals(user.getPassword())) {
                //密码错误
                throw new PasswordErrorException("密码有问题");
            }
            loginId = user.getId();
            name = user.getNickname();

            // 根据user.id去member表查有没有会员记录
            Member member = memberMapper.selectByUserId(user.getId());
            if (member != null) {
                //存在会员记录 → 真实角色为会员
                realRole = 3;
            } else {
                //没有会员记录 → 普通用户
                realRole = 2;
            }
        } else {
            return Results.failure("500", "角色参数错误");
        }

        // JWT载荷，存放真实角色
        Map<String, Object> claims = new HashMap<>();
        claims.put("loginId", loginId);
        claims.put("role", realRole);
        claims.put("username", username);
        String token = jwtUtils.generateJwt(claims);

       //返回真实角色给前端，前端根据这个渲染不同菜单页面


        return Results.success( Loginresult.builder()
                .id(loginId)
                .username(username)
                .name(name)
                .role(realRole)
                .token(token)
                .build());

    }


}
