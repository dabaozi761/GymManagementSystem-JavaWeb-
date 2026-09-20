package com.dabaozi.gymmanagementsystem.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dabaozi.gymmanagementsystem.common.database.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;

/**
 * 会员实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("member")
public class Member extends BaseDO {

    /**
     * 会员编号(业务号,如 M202609200001)
     */
    private String memberId;

    /**
     * 关联用户ID(可空:线下会员可无账号)
     */
    private Long userId;

    /**
     * 姓名
     */
    private String name;

    /**
     * 性别:1 男 2 女
     */
    private Integer gender;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 出生日期
     */
    private LocalDate birthday;

    /**
     * 入会日期
     */
    private LocalDate joinDate;

    /**
     * 状态:1 正常(在籍) 0 已退会
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
