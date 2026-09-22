package com.dabaozi.gymmanagementsystem.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修改管理员入参(password 为空表示不修改密码)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminUpdateDTO {
    private Long id;
    private String name;
    private String phone;
    private String password;
}
