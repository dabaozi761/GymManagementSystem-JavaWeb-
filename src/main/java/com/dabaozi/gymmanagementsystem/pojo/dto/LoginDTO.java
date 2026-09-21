package com.dabaozi.gymmanagementsystem.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginDTO {
    private String username;
    private String password;
    // 1=管理员admin，2=普通用户user
    private Integer role;
}
