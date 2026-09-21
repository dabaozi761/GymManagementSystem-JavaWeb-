package com.dabaozi.gymmanagementsystem.controller;


import com.dabaozi.gymmanagementsystem.common.convention.result.Result;
import com.dabaozi.gymmanagementsystem.common.convention.result.Results;
import com.dabaozi.gymmanagementsystem.dto.LoginDTO;
import com.dabaozi.gymmanagementsystem.service.LoginService;
import com.dabaozi.gymmanagementsystem.vo.Loginresult;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@Slf4j
public class LoginController{
    @Autowired
    private LoginService loginService;

    /**
     * 登录接口
     * @param loginDTO
     * @return
     */

    @PostMapping("login")
    public Result login(@RequestBody LoginDTO loginDTO){
        log.info("登录：{}",loginDTO);
        if(loginDTO.getUsername()==null||loginDTO.getPassword()==null||loginDTO.getRole()==null){
            return Results.failure("401","账号、密码、角色不能为空");
        }
        return loginService.login(loginDTO);


    }



}
