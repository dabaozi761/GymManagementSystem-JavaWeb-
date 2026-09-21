package com.dabaozi.gymmanagementsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dabaozi.gymmanagementsystem.common.convention.result.Result;
import com.dabaozi.gymmanagementsystem.pojo.dto.LoginDTO;

public interface LoginService  {
    /**
     * 登录接口
     * @param loginDTO
     * @return
     */
    Result login(LoginDTO loginDTO);
}
