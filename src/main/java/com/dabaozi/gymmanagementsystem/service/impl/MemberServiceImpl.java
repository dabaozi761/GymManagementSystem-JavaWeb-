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
import com.dabaozi.gymmanagementsystem.pojo.dto.MemberPageReqDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.MemberSaveDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.MemberUpdateDTO;
import com.dabaozi.gymmanagementsystem.pojo.entity.Member;
import com.dabaozi.gymmanagementsystem.pojo.entity.MembershipCard;
import com.dabaozi.gymmanagementsystem.pojo.vo.MemberPageRespVO;
import com.dabaozi.gymmanagementsystem.pojo.vo.MemberVO;
import com.dabaozi.gymmanagementsystem.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements MemberService {

    private final MembershipCardMapper membershipCardMapper;

    @Override
    public String saveMember(MemberSaveDTO dto) {
        validateMember(dto.getName(), dto.getPhone(), dto.getGender(), dto.getStatus());
        checkUniqueness(dto.getPhone(), dto.getUserId(), null);

        Member member = Member.builder()
                .memberId(BusinessNoGenerator.nextMemberNo())
                .userId(dto.getUserId())
                .name(dto.getName().trim())
                .gender(dto.getGender())
                .phone(dto.getPhone().trim())
                .birthday(dto.getBirthday())
                .joinDate(dto.getJoinDate() == null ? LocalDate.now() : dto.getJoinDate())
                .status(dto.getStatus() == null ? 1 : dto.getStatus())
                .remark(dto.getRemark())
                .build();
        baseMapper.insert(member);
        return member.getMemberId();
    }

    @Override
    public void updateMember(MemberUpdateDTO dto) {
        if (dto.getId() == null) {
            throw new ClientException("会员ID不能为空");
        }
        requireMember(dto.getId());
        validateMember(dto.getName(), dto.getPhone(), dto.getGender(), dto.getStatus());
        checkUniqueness(dto.getPhone(), dto.getUserId(), dto.getId());

        Member member = Member.builder()
                .userId(dto.getUserId())
                .name(dto.getName().trim())
                .gender(dto.getGender())
                .phone(dto.getPhone().trim())
                .birthday(dto.getBirthday())
                .joinDate(dto.getJoinDate())
                .status(dto.getStatus() == null ? 1 : dto.getStatus())
                .remark(dto.getRemark())
                .build();
        LambdaUpdateWrapper<Member> updateWrapper= Wrappers.lambdaUpdate(Member.class)
                .eq(Member::getId,dto.getId());
        int rows = baseMapper.update(member, updateWrapper);
        if (rows == 0) {
            throw new ClientException("会员不存在或已删除");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMember(Long id) {
        requireMember(id);
        LambdaQueryWrapper updateMembershipCardWrapper=Wrappers.lambdaQuery(MembershipCard.class)
                        .eq(MembershipCard::getMemberId,id);
        membershipCardMapper.delete(updateMembershipCardWrapper);
        if (baseMapper.deleteById(id) == 0) {
            throw new ClientException("会员不存在或已删除");
        }
    }

    @Override
    public void updateMemberStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new ClientException("会员状态只能是0或1");
        }
        LambdaUpdateWrapper<Member> wrapper = Wrappers.lambdaUpdate(Member.class)
                .eq(Member::getId, id)
                .set(Member::getStatus, status);
        if (baseMapper.update(null, wrapper) == 0) {
            throw new ClientException("会员不存在或已删除");
        }
    }
    //TODO 获取详细信息和分页查询待看
    @Override
    public MemberVO getMember(Long id) {
        return BeanUtil.toBean(requireMember(id), MemberVO.class);
    }

    @Override
    public MemberPageRespVO pageMember(MemberPageReqDTO dto) {
        if (dto == null) {
            dto = new MemberPageReqDTO();
        }
       //当前页数
        //当前页数展示多少条数据
        Long current = dto.getCurrent();
        Long pageSize = dto.getSize();
        Page<Member> page=new Page<>(current,pageSize);
        LambdaQueryWrapper<Member> wrapper = Wrappers.lambdaQuery(Member.class)
                .like(StringUtils.hasText(dto.getMemberId()), Member::getMemberId, dto.getMemberId())
                .like(StringUtils.hasText(dto.getName()), Member::getName, dto.getName())
                .like(StringUtils.hasText(dto.getPhone()), Member::getPhone, dto.getPhone())
                .eq(dto.getStatus() != null, Member::getStatus, dto.getStatus())
                .orderByDesc(Member::getCreateTime);
        IPage<Member> memberIPage=baseMapper.selectPage(page,wrapper);
        List<MemberVO> MemberVOList=memberIPage.getRecords().stream()
                .map(member  ->BeanUtil.toBean(member,MemberVO.class))
                .collect(Collectors.toList());
        return MemberPageRespVO.builder()
                .total(memberIPage.getTotal())
                .records(MemberVOList)
                .current(memberIPage.getCurrent())
                .size(memberIPage.getSize())
                .pages(memberIPage.getPages())
                .build();
    }

    private Member requireMember(Long id) {
        if (id == null) {
            throw new ClientException("会员ID不能为空");
        }
        Member member = baseMapper.selectById(id);
        if (member == null) {
            throw new ClientException("会员不存在或已删除");
        }
        return member;
    }

    private void validateMember(String name, String phone, Integer gender, Integer status) {
        if (!StringUtils.hasText(name)) {
            throw new ClientException("会员姓名不能为空");
        }
        if (!StringUtils.hasText(phone)) {
            throw new ClientException("联系电话不能为空");
        }
        if (gender != null && gender != 1 && gender != 2) {
            throw new ClientException("性别只能是1或2");
        }
        if (status != null && status != 0 && status != 1) {
            throw new ClientException("会员状态只能是0或1");
        }
    }

    /**
     * 检验会员账号的唯一性
     * @param phone
     * @param userId
     * @param excludedId
     */
    private void checkUniqueness(String phone, Long userId, Long excludedId) {
        LambdaQueryWrapper<Member> phoneQuery = Wrappers.lambdaQuery(Member.class)
                .eq(Member::getPhone, phone.trim())
                .ne(excludedId != null, Member::getId, excludedId);
        if (baseMapper.selectCount(phoneQuery) > 0) {
            throw new ClientException("联系电话已被其他会员使用");
        }
        if (userId != null) {
            LambdaQueryWrapper<Member> userQuery = Wrappers.lambdaQuery(Member.class)
                    .eq(Member::getUserId, userId)
                    .ne(excludedId != null, Member::getId, excludedId);
            if (baseMapper.selectCount(userQuery) > 0) {
                throw new ClientException("该用户账号已关联会员");
            }
        }
    }
}
