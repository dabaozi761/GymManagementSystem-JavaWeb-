package com.dabaozi.gymmanagementsystem.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Loginresult {
    private Integer id;
    private String username;
    private String name;
    private String token;
}
