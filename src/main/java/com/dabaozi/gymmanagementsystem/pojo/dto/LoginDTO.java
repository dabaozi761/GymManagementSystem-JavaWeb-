package com.dabaozi.gymmanagementsystem.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginDTO {
    private String username;
    private String password;
    // 1=管理员admin，2=普通用户user，3=会员member
    private Integer role;
}
