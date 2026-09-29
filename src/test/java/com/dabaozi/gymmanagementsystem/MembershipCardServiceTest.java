package com.dabaozi.gymmanagementsystem;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.dabaozi.gymmanagementsystem.common.convention.exception.ClientException;
import com.dabaozi.gymmanagementsystem.mapper.MemberMapper;
import com.dabaozi.gymmanagementsystem.mapper.MembershipCardMapper;
import com.dabaozi.gymmanagementsystem.pojo.dto.MembershipCardConsumeDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.MembershipCardSaveDTO;
import com.dabaozi.gymmanagementsystem.pojo.entity.Member;
import com.dabaozi.gymmanagementsystem.pojo.entity.MembershipCard;
import com.dabaozi.gymmanagementsystem.service.impl.MemberServiceCardImpl;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MembershipCardServiceTest {
    private MemberMapper memberMapper;
    private MembershipCardMapper cardMapper;
    private MemberServiceCardImpl service;

    @BeforeEach
    void setUp() {
        memberMapper = mock(MemberMapper.class);
        cardMapper = mock(MembershipCardMapper.class);
        service = new MemberServiceCardImpl(memberMapper);
        ReflectionTestUtils.setField(service, "baseMapper", cardMapper);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), MembershipCard.class);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void countCardDeductsCountWithoutRequiringAnAmount() {
        MembershipCard card = MembershipCard.builder().cardType(4).status(1).totalCount(20).remainCount(12).build();
        card.setId(2L);
        when(cardMapper.selectById(2L)).thenReturn(card);
        when(cardMapper.update(isNull(), any(Wrapper.class))).thenReturn(1);
        MembershipCardConsumeDTO dto = new MembershipCardConsumeDTO();
        dto.setId(2L);
        dto.setCount(2);

        service.consume(dto);

        ArgumentCaptor<Wrapper<MembershipCard>> updates = ArgumentCaptor.forClass(Wrapper.class);
        verify(cardMapper, times(2)).update(isNull(), updates.capture());
        Wrapper<MembershipCard> deduction = updates.getAllValues().get(1);
        assertEquals("remain_count=remain_count-2", deduction.getSqlSet());
        assertTrue(deduction.getSqlSegment().contains("remain_count >="));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void countCardRejectsInsufficientRemainingCount() {
        MembershipCard card = MembershipCard.builder().cardType(4).status(1).remainCount(1).build();
        card.setId(2L);
        when(cardMapper.selectById(2L)).thenReturn(card);
        when(cardMapper.update(isNull(), any(Wrapper.class))).thenReturn(0);
        MembershipCardConsumeDTO dto = new MembershipCardConsumeDTO();
        dto.setId(2L);
        dto.setCount(2);

        assertThrows(ClientException.class, () -> service.consume(dto));
    }

    @Test
    void withdrawnMemberCannotReceiveANormalCard() {
        Member member = Member.builder().status(0).build();
        when(memberMapper.selectById(1L)).thenReturn(member);
        MembershipCardSaveDTO dto = new MembershipCardSaveDTO();
        dto.setMemberId(1L);
        dto.setCardType(5);
        dto.setStatus(1);

        ClientException error = assertThrows(ClientException.class, () -> service.savaCard(dto));
        assertTrue(error.getMessage().contains("退会会员"));
        verify(cardMapper, never()).insert(any(MembershipCard.class));
    }
}
