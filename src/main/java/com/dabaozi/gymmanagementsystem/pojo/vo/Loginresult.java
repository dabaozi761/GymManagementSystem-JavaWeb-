package com.dabaozi.gymmanagementsystem.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loginresult {
    private Long id;
    private String username;
    private String name;
    private String token;
    private Integer role;
}
