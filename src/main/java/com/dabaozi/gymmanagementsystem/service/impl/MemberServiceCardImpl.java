package com.dabaozi.gymmanagementsystem.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dabaozi.gymmanagementsystem.common.convention.exception.ClientException;
import com.dabaozi.gymmanagementsystem.common.convention.utils.BusinessNoGenerator;
import com.dabaozi.gymmanagementsystem.mapper.MemberMapper;
import com.dabaozi.gymmanagementsystem.mapper.MembershipCardMapper;
import com.dabaozi.gymmanagementsystem.pojo.dto.*;
import com.dabaozi.gymmanagementsystem.pojo.entity.Member;
import com.dabaozi.gymmanagementsystem.pojo.entity.MembershipCard;
import com.dabaozi.gymmanagementsystem.pojo.vo.MembershipCardPageRespVO;
import com.dabaozi.gymmanagementsystem.pojo.vo.MembershipCardVO;
import com.dabaozi.gymmanagementsystem.service.WalletService;
import com.dabaozi.gymmanagementsystem.common.convention.utils.CurrentActor;
import com.dabaozi.gymmanagementsystem.service.MembershipCardService;
//import jdk.javadoc.internal.doclets.toolkit.builders.MemberSummaryBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class MemberServiceCardImpl extends ServiceImpl<MembershipCardMapper, MembershipCard> implements MembershipCardService {

    private static final int MONTH_CARD = 1;
    private static final int QUARTER_CARD = 2;
    private static final int YEAR_CARD = 3;
    private static final int COUNT_CARD = 4;
    private static final int NORMAL_STATUS = 1;

    private final MemberMapper memberMapper;
    private final WalletService walletService;
    private final CurrentActor actor;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String savaCard(MembershipCardSaveDTO membershipCardSaveDTO) {
        actor.requireAdmin();
        Member member = requireMember(membershipCardSaveDTO.getMemberId());
        if (!Integer.valueOf(1).equals(member.getStatus())) {
            throw new ClientException("退会会员不能办理会员卡");
        }
        //构建membershipCard对象
        MembershipCard membershipCard = MembershipCard.builder()
                .cardNo(BusinessNoGenerator.nextCardNo()).build();
        //将dtd中的属性赋值到membershipCard上
        BeanUtils.copyProperties(membershipCardSaveDTO, membershipCard);
        //
        normalizeAndValidate(membershipCard);
        //baseMapper是ServiceImpl中的对象
        baseMapper.insert(membershipCard);
        walletService.recordOpeningBalance(membershipCard);
        return membershipCard.getCardNo();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCard(MembershipCardUpdateDTO dto) {
        actor.requireAdmin();
        if (dto.getId() == null) {
            throw new ClientException("会员卡id不能为空");
        }
        MembershipCard existing = baseMapper.selectForUpdate(dto.getId());
        if (existing == null) throw new ClientException("会员卡不存在或已删除");
        if (!java.util.Objects.equals(existing.getMemberId(), dto.getMemberId())) {
            throw new ClientException("会员卡所属会员不能修改，请另行办卡");
        }
        requireMember(dto.getMemberId());
        if (dto.getBalance() != null && dto.getBalance().compareTo(existing.getBalance() == null ? BigDecimal.ZERO : existing.getBalance()) != 0) {
            throw new ClientException("请通过充值或扣费操作调整余额");
        }
        MembershipCard card = MembershipCard.builder()
                .status(dto.getStatus() == null ? NORMAL_STATUS : dto.getStatus())
                .build();
        BeanUtils.copyProperties(dto, card);
        card.setBalance(existing.getBalance());
        normalizeAndValidate(card);
        card.setBalance(null); // 编辑资料不写余额，避免绕过流水。
        LambdaUpdateWrapper<MembershipCard> wrapper = Wrappers.lambdaUpdate(MembershipCard.class)
                .eq(MembershipCard::getId, dto.getId());
        if (isTimeCard(card.getCardType())) {
            wrapper.set(MembershipCard::getTotalCount, null).set(MembershipCard::getRemainCount, null);
        } else {
            wrapper.set(MembershipCard::getStartDate, null).set(MembershipCard::getEndDate, null);
        }
        //因为前面card已经赋值过了，所以要带上card :et
        baseMapper.update(card, wrapper);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCard(Long id) {
        actor.requireAdmin();
        if (id == null) throw new ClientException("会员卡ID不能为空");
        MembershipCard card = baseMapper.selectForUpdate(id);
        if (card == null) throw new ClientException("会员卡不存在或已删除");
        if (card.getBalance() != null && card.getBalance().signum() > 0) throw new ClientException("有余额的会员卡不能删除");
        if (baseMapper.deleteById(id) == 0) {
            throw new ClientException("会员卡不存在或已删除");
        }
    }

    @Override
    public void updateCardStatus(Long id, Integer status) {
        actor.requireAdmin();
        if (status == null || status < 1 || status > 4) {
            throw new ClientException("会员卡状态只能是1至4");
        }
        MembershipCard card = requireCard(id);
        //TODO这个业务逻辑再理清楚
        if (status == NORMAL_STATUS && isTimeCard(card.getCardType())
                && card.getEndDate() != null && card.getEndDate().isBefore(LocalDate.now())) {
            throw new ClientException("已经过有效期的会员卡不能恢复成正常状态");
        }
        if (baseMapper.update(null, Wrappers.lambdaUpdate(MembershipCard.class)
                .eq(MembershipCard::getId, id)
                .set(MembershipCard::getStatus, status)) == 0) {
            throw new ClientException("会员不存在或已删除");

        }
    }

    @Override
    public void recharge(MembershipCardRechargeDTO dto) {
        if (dto == null) throw new ClientException("充值参数不能为空");
        walletService.recharge(dto.getId(), dto.getAmount(), dto.getRemark());
    }

    @Override
    public void consume(MembershipCardConsumeDTO dto) {
        if (dto == null) throw new ClientException("消费参数不能为空");
        walletService.consume(dto.getId(), dto.getAmount(), dto.getCount(), dto.getRemark());
    }

    @Override
    public MembershipCardVO getCard(Long id) {
        //检验这个卡是否有效，是不是正常状态
        MembershipCard card= requireCard(id);
        actor.requireOwner(card.getMemberId());
        //根据会员id查询会员
        Member member=memberMapper.selectById(card.getMemberId());
        //将MembershipCard的值复制粘贴到vo
        MembershipCardVO membershipCardVO= BeanUtil.toBean(card,MembershipCardVO.class);
        if(member!=null){
            //设置会员编号
            membershipCardVO.setMemberNo(member.getMemberId());
            membershipCardVO.setMemberName(member.getName());
        }

        return membershipCardVO;


    }

    @Override
    public MembershipCardPageRespVO pageCard(MembershipCardPageReqDTO dto) {
        refreshExpiredCards();//刷新卡
        if(dto==null){
            dto=new MembershipCardPageReqDTO();//空指针保护
        }

        if (dto.getCurrent() < 1 || dto.getSize() < 1 || dto.getSize() > 100) throw new ClientException("分页参数不合法");
        dto.setMemberId(actor.scopedMemberId(dto.getMemberId()));
        LambdaQueryWrapper<MembershipCard> wrapper=Wrappers.lambdaQuery(MembershipCard.class)
                .like(dto.getCardNo()!=null,MembershipCard::getCardNo,dto.getCardNo())
                .eq(dto.getMemberId()!=null,MembershipCard::getMemberId,dto.getMemberId())
                .eq(dto.getCardType()!=null,MembershipCard::getCardType,dto.getCardType())
                .eq(dto.getStatus() != null, MembershipCard::getStatus, dto.getStatus())
                .orderByDesc(MembershipCard::getCreateTime);
        //wrapper查询条件
        //当前页数
        //当前页数展示多少条数据
        Page<MembershipCard> page = new Page<>(dto.getCurrent(), dto.getSize());
        IPage<MembershipCard> pageResult = baseMapper.selectPage(page, wrapper);
        List<Long> memberIds=pageResult.getRecords().stream().map(MembershipCard::getMemberId).distinct().toList();
        Map<Long, Member> memberMap = memberIds.isEmpty()
                ? Collections.emptyMap()
                : memberMapper.selectBatchIds(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));

        // 第三步：会员卡实体转 MembershipCardVO
        List<MembershipCardVO> voList = page.getRecords().stream()
                .map(card -> toVO(card, memberMap))
                .toList();

        //  组装自定义的 MembershipCardPageRespVO，用builder
        return MembershipCardPageRespVO.builder()
                .total(page.getTotal())
                .records(voList)
                .current(pageResult.getCurrent())
                .size(pageResult.getSize())
                .pages(pageResult.getPages())
                .build();

    }


    /**
     * 根据memberid检验会员是否存在
     *
     * @param id
     * @return
     */
    private Member requireMember(Long id) {
        if (id == null) {
            throw new ClientException("所属会员ID不能为空");
        }
        Member member = memberMapper.selectById(id);
        if (member == null) {
            throw new ClientException("所属会员不存在或已删除");
        }
        return member;
    }

    private MembershipCard requireCard(Long id) {
        if (id == null) {
            throw new ClientException("会员卡ID不能为空");
        }
        MembershipCard card = baseMapper.selectById(id);
        if (card == null) {
            throw new ClientException("会员卡不存在或已删除");
        }
        return card;
    }

    private void normalizeAndValidate(MembershipCard card) {
        Integer type = card.getCardType();
        if (card.getBalance() == null) card.setBalance(BigDecimal.ZERO);
        WalletService.validateMoney(card.getBalance(), true);
        if (card.getStatus() == null) card.setStatus(NORMAL_STATUS);
        if (type == null || type < MONTH_CARD || type > COUNT_CARD) {
            throw new ClientException("会员卡类型只能是1至4");
        }
        if (card.getStatus() == null || card.getStatus() < 1 || card.getStatus() > 4) {
            throw new ClientException("会员卡状态只能是1至4");
        }

        if (isTimeCard(type)) {
            if (card.getStartDate() == null || card.getEndDate() == null) {
                throw new ClientException("月卡、季卡和年卡必须填写有效期");
            }
            if (card.getEndDate().isBefore(card.getStartDate())) {
                throw new ClientException("有效期结束日期不能早于开始日期");
            }
            card.setTotalCount(null);
            card.setRemainCount(null);
        } else if (Integer.valueOf(COUNT_CARD).equals(type)) {
            if (card.getTotalCount() == null || card.getTotalCount() <= 0) {
                throw new ClientException("次卡总次数必须大于0");
            }
            if (card.getRemainCount() == null) {
                card.setRemainCount(card.getTotalCount());
            }
            if (card.getRemainCount() < 0 || card.getRemainCount() > card.getTotalCount()) {
                throw new ClientException("次卡剩余次数必须在0到总次数之间");
            }
            card.setStartDate(null);
            card.setEndDate(null);

        }
    }

    /**
     * 判断会员卡的类型是月卡、季卡、年卡这些计时卡
     *
     * @param type
     * @return
     */
    private boolean isTimeCard(Integer type) {
        return Integer.valueOf(MONTH_CARD).equals(type)
                || Integer.valueOf(QUARTER_CARD).equals(type)
                || Integer.valueOf(YEAR_CARD).equals(type);
    }

    /**
     * 自动把已过期的月卡、季卡、年卡标记为状态3。
     */
    private void refreshExpiredCards() {
        baseMapper.update(null, Wrappers.lambdaUpdate(MembershipCard.class)
                .in(MembershipCard::getCardType, MONTH_CARD, QUARTER_CARD, YEAR_CARD)
                .eq(MembershipCard::getStatus, NORMAL_STATUS)
                .lt(MembershipCard::getEndDate, LocalDate.now())
                .set(MembershipCard::getStatus, 3));
    }

    /**
     * 将MembershipCard转化成MembershipCardVO
     * @param card
     * @param memberMap
     * @return
     */
    private MembershipCardVO toVO(MembershipCard card, Map<Long, Member> memberMap) {
        MembershipCardVO vo = BeanUtil.toBean(card, MembershipCardVO.class);
        Member member = memberMap.get(card.getMemberId());
        if (member != null) {
            vo.setMemberNo(member.getMemberId());
            vo.setMemberName(member.getName());
        }
        return vo;
    }
}
