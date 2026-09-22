package com.dabaozi.gymmanagementsystem.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理员列表/详情返回(不含密码)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminVO {
    private Long id;
    private String username;
    private String name;
    private String phone;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
