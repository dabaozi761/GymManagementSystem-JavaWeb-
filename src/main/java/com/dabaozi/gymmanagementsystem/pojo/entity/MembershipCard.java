package com.dabaozi.gymmanagementsystem.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dabaozi.gymmanagementsystem.common.database.BaseDO;
import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 会员卡实体(月卡/季卡/年卡/次卡/储值卡统一一张表,按 cardType 区分使用哪组字段)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("membership_card")
public class MembershipCard extends BaseDO {

    /**
     * 卡号(唯一)
     */
    private String cardNo;

    /**
     * 所属会员ID
     */
    private Long memberId;

    /**
     * 卡类型:1 月卡 2 季卡 3 年卡 4 次卡 5 储值卡
     */
    private Integer cardType;

    /**
     * 余额(储值卡使用)
     */
    private BigDecimal balance;

    /**
     * 总次数(次卡使用)
     */
    private Integer totalCount;

    /**
     * 剩余次数(次卡使用)
     */
    private Integer remainCount;

    /**
     * 有效期开始(月/季/年卡使用)
     */
    private LocalDate startDate;

    /**
     * 有效期结束(月/季/年卡使用)
     */
    private LocalDate endDate;

    /**
     * 状态:1 正常 2 冻结 3 过期 4 注销
     */
    private Integer status;
}
