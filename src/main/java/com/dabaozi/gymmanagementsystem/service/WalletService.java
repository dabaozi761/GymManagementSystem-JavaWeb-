package com.dabaozi.gymmanagementsystem.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dabaozi.gymmanagementsystem.common.convention.exception.ClientException;
import com.dabaozi.gymmanagementsystem.common.convention.utils.CurrentActor;
import com.dabaozi.gymmanagementsystem.mapper.*;
import com.dabaozi.gymmanagementsystem.pojo.dto.CommercePageReqDTO;
import com.dabaozi.gymmanagementsystem.pojo.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {
    public static final BigDecimal MAX_MONEY = new BigDecimal("99999999.99");
    private final MembershipCardMapper cardMapper;
    private final MemberMapper memberMapper;
    private final CardTransactionMapper transactionMapper;
    private final CurrentActor actor;

    public static void validateMoney(BigDecimal amount, boolean allowZero) {
        if (amount == null || amount.signum() < (allowZero ? 0 : 1)
                || amount.compareTo(MAX_MONEY) > 0 || amount.stripTrailingZeros().scale() > 2) {
            throw new ClientException("金额必须" + (allowZero ? "不小于0" : "大于0") + "，最多两位小数且不超过99999999.99元");
        }
    }

    /** 调用方必须处于事务中；同一卡的操作串行，保证余额和流水快照一致。 */
    public MembershipCard lockUsableCard(Long id) {
        if (id == null) throw new ClientException("会员卡ID不能为空");
        MembershipCard card = cardMapper.selectForUpdate(id);
        if (card == null) throw new ClientException("会员卡不存在或已删除");
        Member member = memberMapper.selectById(card.getMemberId());
        if (member == null || !Integer.valueOf(1).equals(member.getStatus())) throw new ClientException("会员已退会或不存在");
        if (!Integer.valueOf(1).equals(card.getStatus())) throw new ClientException("会员卡不是正常状态");
        if (card.getCardType() == null || card.getCardType() < 1 || card.getCardType() > 4) throw new ClientException("仅支持1至4类型会员卡，请先调整旧卡类型");
        if (card.getCardType() <= 3 && (card.getStartDate() == null || card.getEndDate() == null
                || LocalDate.now().isBefore(card.getStartDate()) || LocalDate.now().isAfter(card.getEndDate()))) {
            throw new ClientException("会员卡尚未生效或已过期");
        }
        return card;
    }

    @Transactional(rollbackFor = Exception.class)
    public void recharge(Long id, BigDecimal amount, String remark) {
        actor.requireAdmin(); // 充值由前台确认收款，会员不能凭空增加余额。
        validateMoney(amount, false);
        changeBalance(lockUsableCard(id), amount, 1, null, remark);
    }

    @Transactional(rollbackFor = Exception.class)
    public void consume(Long id, BigDecimal amount, Integer count, String remark) {
        actor.requireAdmin();
        if ((amount == null) == (count == null)) throw new ClientException("消费金额和核销次数必须且只能填写一项");
        if (amount != null) validateMoney(amount, false);
        MembershipCard card = lockUsableCard(id);
        if (amount != null) {
            changeBalance(card, amount.negate(), 2, null, remark);
        } else {
            if (card.getCardType() != 4 || count <= 0) throw new ClientException("只有次卡支持核销正整数次数");
            if (card.getRemainCount() == null || card.getRemainCount() < count) throw new ClientException("会员卡剩余次数不够");
            CardTransaction entry = entry(card, 3, remark);
            entry.setAmount(BigDecimal.ZERO);
            entry.setCount(count);
            entry.setRemainCountBefore(card.getRemainCount());
            entry.setRemainCountAfter(card.getRemainCount() - count);
            if (cardMapper.update(null, Wrappers.lambdaUpdate(MembershipCard.class).eq(MembershipCard::getId, id)
                    .set(MembershipCard::getRemainCount, entry.getRemainCountAfter())) != 1) throw new ClientException("核销失败");
            transactionMapper.insert(entry);
        }
    }

    /** 用于购物扣费：卡已加行锁，订单、库存、扣费和流水处于同一事务。 */
    public void changeBalance(MembershipCard card, BigDecimal delta, int type, Long orderId, String remark) {
        CardTransaction entry = entry(card, type, remark);
        BigDecimal after = entry.getBalanceBefore().add(delta);
        if (after.signum() < 0) throw new ClientException("会员卡余额不足");
        validateMoney(after, true);
        entry.setAmount(delta.abs());
        entry.setBalanceAfter(after);
        entry.setOrderId(orderId);
        if (cardMapper.update(null, Wrappers.lambdaUpdate(MembershipCard.class).eq(MembershipCard::getId, card.getId())
                .set(MembershipCard::getBalance, after)) != 1) throw new ClientException("余额更新失败");
        transactionMapper.insert(entry);
        card.setBalance(after);
    }

    public void recordOpeningBalance(MembershipCard card) {
        if (card.getBalance().signum() == 0) return;
        CardTransaction entry = entry(card, 1, "办卡初始充值");
        entry.setAmount(card.getBalance());
        entry.setBalanceBefore(BigDecimal.ZERO);
        transactionMapper.insert(entry);
    }

    private CardTransaction entry(MembershipCard card, int type, String remark) {
        if (remark != null && remark.length() > 255) throw new ClientException("备注最多255字");
        CardTransaction entry = new CardTransaction();
        entry.setTransactionNo("TX" + UUID.randomUUID().toString().replace("-", ""));
        entry.setCardId(card.getId()); entry.setCardNo(card.getCardNo()); entry.setMemberId(card.getMemberId());
        Member member = memberMapper.selectById(card.getMemberId());
        entry.setMemberName(member == null ? "会员" : member.getName());
        entry.setType(type); entry.setRemark(remark);
        BigDecimal balance = card.getBalance() == null ? BigDecimal.ZERO : card.getBalance();
        entry.setBalanceBefore(balance); entry.setBalanceAfter(balance);
        entry.setOperatorId(actor.loginId()); entry.setOperatorRole(actor.role());
        return entry;
    }

    public Page<CardTransaction> page(CommercePageReqDTO dto) {
        dto.validate();
        Long memberId = actor.scopedMemberId(dto.getMemberId());
        return transactionMapper.selectPage(new Page<>(dto.getCurrent(), dto.getSize()), Wrappers.lambdaQuery(CardTransaction.class)
                .eq(memberId != null, CardTransaction::getMemberId, memberId)
                .eq(dto.getCardId() != null, CardTransaction::getCardId, dto.getCardId())
                .eq(dto.getType() != null, CardTransaction::getType, dto.getType())
                .ge(dto.getFromTime() != null, CardTransaction::getCreateTime, dto.getFromTime())
                .le(dto.getToTime() != null, CardTransaction::getCreateTime, dto.getToTime())
                .orderByDesc(CardTransaction::getId));
    }

    public CardTransaction detail(Long id) {
        CardTransaction entry = transactionMapper.selectById(id);
        if (entry == null) throw new ClientException("流水不存在");
        actor.requireOwner(entry.getMemberId());
        return entry;
    }
}
