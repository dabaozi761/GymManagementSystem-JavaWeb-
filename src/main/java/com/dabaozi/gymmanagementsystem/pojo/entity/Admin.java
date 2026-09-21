package com.dabaozi.gymmanagementsystem.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dabaozi.gymmanagementsystem.common.database.BaseDO;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * 管理员实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("admin")
public class Admin extends BaseDO {

    /**
     * 管理员用户名(唯一)
     */
    private String username;

    /**
     * 密码(加密存储)
     */
    private String password;

    /**
     * 姓名
     */
    private String name;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 状态:1 正常 0 禁用
     */
    private Integer status;
}



