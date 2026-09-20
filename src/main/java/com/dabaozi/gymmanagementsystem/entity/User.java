package com.dabaozi.gymmanagementsystem.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dabaozi.gymmanagementsystem.common.database.BaseDO;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * 用户实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("user")
public class User extends BaseDO {

    /**
     * 用户名(唯一)
     */
    private String username;

    /**
     * 密码(加密存储)
     */
    private String password;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 状态:1 正常 0 禁用
     */
    private Integer status;
}
