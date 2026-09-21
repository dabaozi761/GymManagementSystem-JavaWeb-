package com.dabaozi.gymmanagementsystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    //接口的统一命名格式请改为："api/gym-management-system/类名/v1/接口名"
    //eg:api/gym-management-system/user/v1/login
    @GetMapping("/hi")
    public String Hi(){
        return "ok";
    }
}
