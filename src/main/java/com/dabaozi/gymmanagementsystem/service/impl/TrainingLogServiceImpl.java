package com.dabaozi.gymmanagementsystem.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dabaozi.gymmanagementsystem.common.convention.exception.ClientException;
import com.dabaozi.gymmanagementsystem.mapper.MemberMapper;
import com.dabaozi.gymmanagementsystem.mapper.TrainingLogMapper;
import com.dabaozi.gymmanagementsystem.mapper.TrainingPlanMapper;
import com.dabaozi.gymmanagementsystem.pojo.dto.TrainingLogDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.TrainingLogPageReqDTO;
import com.dabaozi.gymmanagementsystem.pojo.entity.Member;
import com.dabaozi.gymmanagementsystem.pojo.entity.TrainingLog;
import com.dabaozi.gymmanagementsystem.pojo.entity.TrainingPlan;
import com.dabaozi.gymmanagementsystem.pojo.vo.TrainingLogPageRespDTO;
import com.dabaozi.gymmanagementsystem.service.TrainingLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 训练记录服务实现(非管理员只能操作/查询本人计划下的记录)
 */
@Service
@RequiredArgsConstructor
public class TrainingLogServiceImpl extends ServiceImpl<TrainingLogMapper,TrainingLog> implements TrainingLogService {

    /**
     * 角色:管理员
     */
    private static final int ROLE_ADMIN = 1;

    /**
     * 会员状态:在籍
     */
    private static final int MEMBER_NORMAL = 1;

    private final MemberMapper memberMapper;

    private final TrainingPlanMapper trainingPlanMapper;

    @Override
    public void save(TrainingLogDTO trainingLogDTO, Long loginId, Integer role) {
        requirePlanOwned(trainingLogDTO.getTrainingPlanId(), loginId, role);
        TrainingLog trainingLog = TrainingLog.builder()
                .content(trainingLogDTO.getContent())
                .remark(trainingLogDTO.getRemark())
                .trainDate(trainingLogDTO.getTrainDate())
                .trainingPlanId(trainingLogDTO.getTrainingPlanId())
                .feeling(trainingLogDTO.getFeeling())
                .durationMinutes(trainingLogDTO.getDurationMinutes())
                .build();
        baseMapper.insert(trainingLog);
    }

    @Override
    public void deleteWithId(Long id, Long loginId, Integer role) {
        TrainingLog existing = requireLog(id);
        requirePlanOwned(existing.getTrainingPlanId(), loginId, role);
        LambdaUpdateWrapper<TrainingLog> updateWrapper = Wrappers.lambdaUpdate(TrainingLog.class)
                .eq(TrainingLog::getId, id)
                .eq(TrainingLog::getDelFlag, 0);
        baseMapper.delete(updateWrapper);
    }

    @Override
    public void updateTrainingLog(TrainingLogDTO trainingLogDTO, Long loginId, Integer role) {
        TrainingLog existing = requireLog(trainingLogDTO.getId());
        requirePlanOwned(existing.getTrainingPlanId(), loginId, role);
        // 改绑的计划同样必须是本人的
        requirePlanOwned(trainingLogDTO.getTrainingPlanId(), loginId, role);
        LambdaUpdateWrapper<TrainingLog> updateWrapper = Wrappers.lambdaUpdate(TrainingLog.class)
                .eq(TrainingLog::getId, trainingLogDTO.getId())
                .eq(TrainingLog::getDelFlag, 0);
        TrainingLog trainingLog = TrainingLog.builder()
                .trainingPlanId(trainingLogDTO.getTrainingPlanId())
                .trainDate(trainingLogDTO.getTrainDate())
                .durationMinutes(trainingLogDTO.getDurationMinutes())
                .content(trainingLogDTO.getContent())
                .feeling(trainingLogDTO.getFeeling())
                .remark(trainingLogDTO.getRemark())
                .build();
        baseMapper.update(trainingLog, updateWrapper);
    }

    @Override
    public IPage<TrainingLogPageRespDTO> pageTrainingLog(TrainingLogPageReqDTO trainingLogPageReqDTO, Long loginId, Integer role) {
        // 非管理员强制只查本人计划下的记录;未关联会员时用不存在的id兜底返回空页
        if (role == null || role != ROLE_ADMIN) {
            Member member = memberMapper.selectByUserId(loginId);
            trainingLogPageReqDTO.setMemberId(member == null ? -1L : member.getId());
        }
        IPage<TrainingLogPageRespDTO> pageResult = baseMapper.pageTrainingLog(trainingLogPageReqDTO)
                .convert(each -> BeanUtil.toBean(each, TrainingLogPageRespDTO.class));
        // 批量补全计划名称,列表展示用
        List<TrainingLogPageRespDTO> records = pageResult.getRecords();
        if (records != null && !records.isEmpty()) {
            Set<Long> planIds = records.stream()
                    .map(TrainingLogPageRespDTO::getTrainingPlanId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (!planIds.isEmpty()) {
                Map<Long, String> titleMap = trainingPlanMapper.selectBatchIds(planIds).stream()
                        .collect(Collectors.toMap(TrainingPlan::getId, TrainingPlan::getTitle, (a, b) -> a));
                records.forEach(record -> record.setPlanTitle(titleMap.get(record.getTrainingPlanId())));
            }
        }
        return pageResult;
    }

    /**
     * 校验记录存在
     */
    private TrainingLog requireLog(Long id) {
        if (id == null) {
            throw new ClientException("训练记录id不能为空");
        }
        TrainingLog existing = baseMapper.selectById(id);
        if (existing == null) {
            throw new ClientException("训练记录不存在或已删除");
        }
        return existing;
    }

    /**
     * 校验计划存在且(非管理员时)属于当前登录会员;计划id为空时仅非管理员报错,管理员维持原行为
     */
    private TrainingPlan requirePlanOwned(Long trainingPlanId, Long loginId, Integer role) {
        if (trainingPlanId == null) {
            if (role == null || role != ROLE_ADMIN) {
                throw new ClientException("训练计划不能为空");
            }
            return null;
        }
        TrainingPlan plan = trainingPlanMapper.selectById(trainingPlanId);
        if (plan == null) {
            throw new ClientException("训练计划不存在或已删除");
        }
        if (role == null || role != ROLE_ADMIN) {
            Member member = requireMemberByLoginId(loginId);
            if (!member.getId().equals(plan.getMemberId())) {
                throw new ClientException("只能操作自己的训练计划");
            }
        }
        return plan;
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
