package com.dabaozi.gymmanagementsystem.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dabaozi.gymmanagementsystem.common.convention.exception.ClientException;
import com.dabaozi.gymmanagementsystem.mapper.MemberMapper;
import com.dabaozi.gymmanagementsystem.mapper.TrainingPlanMapper;
import com.dabaozi.gymmanagementsystem.pojo.dto.TrainingPlanDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.TrainingPlanPageReqDTO;
import com.dabaozi.gymmanagementsystem.pojo.entity.Member;
import com.dabaozi.gymmanagementsystem.pojo.entity.TrainingPlan;
import com.dabaozi.gymmanagementsystem.pojo.vo.TrainingPlanPageRespDTO;
import com.dabaozi.gymmanagementsystem.service.TrainingPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 训练计划服务实现(非管理员只能操作/查询本人的计划)
 */
@Service
@RequiredArgsConstructor
public class TrainingPlanServiceImpl extends ServiceImpl<TrainingPlanMapper, TrainingPlan> implements TrainingPlanService {

    /**
     * 角色:管理员
     */
    private static final int ROLE_ADMIN = 1;

    /**
     * 会员状态:在籍
     */
    private static final int MEMBER_NORMAL = 1;

    /**
     * 计划状态:进行中(会员新建时默认)
     */
    private static final int STATUS_ONGOING = 1;

    private final MemberMapper memberMapper;

    @Override
    public void save(TrainingPlanDTO trainingPlanDTO, Long loginId, Integer role) {
        if (role != null && role == ROLE_ADMIN && trainingPlanDTO.getMemberId() == null) {
            throw new ClientException("会员id不能为空");
        }
        // 非管理员强制绑定本人,不信任前端传参
        if (role == null || role != ROLE_ADMIN) {
            Member member = requireMemberByLoginId(loginId);
            trainingPlanDTO.setMemberId(member.getId());
            trainingPlanDTO.setAdminId(null);
        }
        TrainingPlan trainingPlan = TrainingPlan.builder()
                .startDate(trainingPlanDTO.getStartDate())
                .endDate(trainingPlanDTO.getEndDate())
                .goal(trainingPlanDTO.getGoal())
                .title(trainingPlanDTO.getTitle())
                .adminId(trainingPlanDTO.getAdminId())
                .memberId(trainingPlanDTO.getMemberId())
                .content(trainingPlanDTO.getContent())
                .status(trainingPlanDTO.getStatus() == null ? STATUS_ONGOING : trainingPlanDTO.getStatus())
                .remark(trainingPlanDTO.getRemark())
                .build();
        baseMapper.insert(trainingPlan);
    }

    @Override
    public void deleteWithId(Long id, Long loginId, Integer role) {
        requireOwned(id, loginId, role);
        LambdaUpdateWrapper<TrainingPlan> updateWrapper = Wrappers.lambdaUpdate(TrainingPlan.class)
                .eq(TrainingPlan::getId, id)
                .eq(TrainingPlan::getDelFlag, 0);
        baseMapper.delete(updateWrapper);
    }

    @Override
    public void updateTrainingPlan(TrainingPlanDTO trainingPlanDTO, Long loginId, Integer role) {
        TrainingPlan existing = requireOwned(trainingPlanDTO.getId(), loginId, role);
        // 非管理员不得改归属,保留原会员与制定管理员
        if (role == null || role != ROLE_ADMIN) {
            trainingPlanDTO.setMemberId(existing.getMemberId());
            trainingPlanDTO.setAdminId(existing.getAdminId());
        }
        LambdaUpdateWrapper<TrainingPlan> updateWrapper = Wrappers.lambdaUpdate(TrainingPlan.class)
                .eq(TrainingPlan::getId, trainingPlanDTO.getId())
                .eq(TrainingPlan::getDelFlag, 0);
        TrainingPlan trainingPlan = TrainingPlan.builder()
                .memberId(trainingPlanDTO.getMemberId())
                .adminId(trainingPlanDTO.getAdminId())
                .title(trainingPlanDTO.getTitle())
                .goal(trainingPlanDTO.getGoal())
                .content(trainingPlanDTO.getContent())
                .startDate(trainingPlanDTO.getStartDate())
                .endDate(trainingPlanDTO.getEndDate())
                .status(trainingPlanDTO.getStatus())
                .remark(trainingPlanDTO.getRemark())
                .build();
        baseMapper.update(trainingPlan, updateWrapper);
    }

    @Override
    public IPage<TrainingPlanPageRespDTO> pageTrainingPlan(TrainingPlanPageReqDTO trainingPlanPageReqDTO, Long loginId, Integer role) {
        // 非管理员强制只查本人;未关联会员时用不存在的id兜底返回空页
        if (role == null || role != ROLE_ADMIN) {
            Member member = memberMapper.selectByUserId(loginId);
            trainingPlanPageReqDTO.setMemberId(member == null ? -1L : member.getId());
        }
        IPage<TrainingPlan> pageResult = baseMapper.pageTrainingPlan(trainingPlanPageReqDTO);
        return pageResult.convert(each -> BeanUtil.toBean(each, TrainingPlanPageRespDTO.class));
    }

    /**
     * 校验计划存在且(非管理员时)属于当前登录会员,返回计划实体
     */
    private TrainingPlan requireOwned(Long id, Long loginId, Integer role) {
        if (id == null) {
            throw new ClientException("训练计划id不能为空");
        }
        TrainingPlan existing = baseMapper.selectById(id);
        if (existing == null) {
            throw new ClientException("训练计划不存在或已删除");
        }
        if (role == null || role != ROLE_ADMIN) {
            Member member = requireMemberByLoginId(loginId);
            if (!member.getId().equals(existing.getMemberId())) {
                throw new ClientException("无权操作他人的训练计划");
            }
        }
        return existing;
    }

    /**
     * 根据登录态用户id查找会员,查不到则提示先办理会员
     */
    private Member requireMemberByLoginId(Long loginId) {
        if (loginId == null) {
            throw new ClientException("登录信息缺失");
        }
        Member member = memberMapper.selectByUserId(loginId);
        if (member == null) {
            throw new ClientException("当前账号未关联会员，请先办理会员");
        }
        if (!Integer.valueOf(MEMBER_NORMAL).equals(member.getStatus())) {
            throw new ClientException("会员已退会，无法操作");
        }
        return member;
    }
}
