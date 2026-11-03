package com.dabaozi.gymmanagementsystem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dabaozi.gymmanagementsystem.pojo.dto.CommercePageReqDTO;
import com.dabaozi.gymmanagementsystem.pojo.entity.CardTransaction;
import com.dabaozi.gymmanagementsystem.pojo.entity.MembershipCard;

import java.math.BigDecimal;

/** 会员卡余额、次数核销与流水服务。 */
public interface WalletService {
    BigDecimal MAX_MONEY = new BigDecimal("99999999.99");

    /** 校验金额范围及小数精度。 */
    void validateMoney(BigDecimal amount, boolean allowZero);

    /** 加锁并校验会员卡可用性，调用方必须处于事务中。 */
    MembershipCard lockUsableCard(Long id);

    /** 管理员确认充值并记录流水。 */
    void recharge(Long id, BigDecimal amount, String remark);

    /** 管理员扣减余额或核销次数并记录流水。 */
    void consume(Long id, BigDecimal amount, Integer count, String remark);

    /** 调整已加锁会员卡的余额并记账，沿用调用方事务。 */
    void changeBalance(MembershipCard card, BigDecimal delta, int type, Long orderId, String remark);

    /** 记录办卡初始充值，沿用调用方事务。 */
    void recordOpeningBalance(MembershipCard card);

    /** 按当前账号的数据权限分页查询流水。 */
    Page<CardTransaction> page(CommercePageReqDTO dto);

    /** 查询流水详情并校验数据归属。 */
    CardTransaction detail(Long id);
}
