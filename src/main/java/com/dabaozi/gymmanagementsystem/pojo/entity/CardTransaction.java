package com.dabaozi.gymmanagementsystem.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dabaozi.gymmanagementsystem.common.database.BaseDO;
import lombok.Data;
import java.math.BigDecimal;

/** 不提供修改/删除接口；保存发生时的会员和卡号快照。 */
@Data
@TableName("card_transaction")
public class CardTransaction extends BaseDO {
    private String transactionNo;
    private Long cardId;
    private String cardNo;
    private Long memberId;
    private String memberName;
    private Integer type; // 1充值 2余额消费 3次数核销
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private Integer count;
    private Integer remainCountBefore;
    private Integer remainCountAfter;
    private Long orderId;
    private Long operatorId;
    private Integer operatorRole;
    private String remark;
}
