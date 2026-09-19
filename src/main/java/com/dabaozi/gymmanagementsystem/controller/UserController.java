package com.dabaozi.gymmanagementsystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RestController
public class UserController {

    @GetMapping("/hi")
    public String Hi() {
        return "ok";
    }
    a
}
