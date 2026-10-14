package com.dabaozi.gymmanagementsystem.common.convention.utils;

import com.dabaozi.gymmanagementsystem.common.convention.exception.ClientException;
import com.dabaozi.gymmanagementsystem.mapper.MemberMapper;
import com.dabaozi.gymmanagementsystem.pojo.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

/** 身份只读取已验证的 JWT 请求属性，不接受客户端传入的会员身份。 */
@Component
@RequiredArgsConstructor
public class CurrentActor {
    private final MemberMapper memberMapper;

    private Object attribute(String key) {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs)) {
            throw new ClientException("登录信息缺失");
        }
        Object value = attrs.getRequest().getAttribute(key);
        if (value == null) throw new ClientException("登录信息缺失");
        return value;
    }

    public Long loginId() { return Long.valueOf(attribute("loginId").toString()); }
    public Integer role() { return Integer.valueOf(attribute("role").toString()); }
    public boolean isAdmin() { return role() == 1; }
    public void requireAdmin() {
        if (!isAdmin()) throw new ClientException("仅管理员可以操作");
    }
    public Member member() {
        if (role() != 2 && role() != 3) throw new ClientException("请使用会员账号操作");
        Member member = memberMapper.selectByUserId(loginId());
        if (member == null) throw new ClientException("当前账号未关联会员，请联系前台");
        return member;
    }
    public void requireOwner(Long memberId) {
        if (!isAdmin() && !Objects.equals(member().getId(), memberId)) {
            throw new ClientException("只能查看或操作本人的记录");
        }
    }
    public Long scopedMemberId(Long requested) {
        return isAdmin() ? requested : member().getId();
    }
}
