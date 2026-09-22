package com.dabaozi.gymmanagementsystem.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dabaozi.gymmanagementsystem.mapper.TrainingPlanMapper;
import com.dabaozi.gymmanagementsystem.pojo.dto.TrainingPlanDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.TrainingPlanPageReqDTO;
import com.dabaozi.gymmanagementsystem.pojo.entity.TrainingPlan;
import com.dabaozi.gymmanagementsystem.pojo.vo.TrainingPlanPageRespDTO;
import com.dabaozi.gymmanagementsystem.service.TrainingPlanService;
import org.springframework.stereotype.Service;

@Service
public class TrainingPlanServiceImpl extends ServiceImpl<TrainingPlanMapper, TrainingPlan> implements TrainingPlanService {
    @Override
    public void save(TrainingPlanDTO trainingPlanDTO) {
        TrainingPlan trainingPlan = TrainingPlan.builder()
                .startDate(trainingPlanDTO.getStartDate())
                .endDate(trainingPlanDTO.getEndDate())
                .goal(trainingPlanDTO.getGoal())
                .title(trainingPlanDTO.getTitle())
                .adminId(trainingPlanDTO.getAdminId())
                .memberId(trainingPlanDTO.getMemberId())
                .content(trainingPlanDTO.getContent())
                .status(trainingPlanDTO.getStatus())
                .remark(trainingPlanDTO.getRemark())
                .build();
        baseMapper.insert(trainingPlan);
    }

    @Override
    public void deleteWithId(Long id) {
        LambdaUpdateWrapper<TrainingPlan> updateWrapper = Wrappers.lambdaUpdate(TrainingPlan.class)
                .eq(TrainingPlan::getId, id)
                .eq(TrainingPlan::getDelFlag, 0);
        baseMapper.delete(updateWrapper);
    }

    @Override
    public void updateTrainingPlan(TrainingPlanDTO trainingPlanDTO) {
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
    public IPage<TrainingPlanPageRespDTO> pageTrainingPlan(TrainingPlanPageReqDTO trainingPlanPageReqDTO) {
        IPage<TrainingPlan> pageResult = baseMapper.pageTrainingPlan(trainingPlanPageReqDTO);
        return pageResult.convert(each -> BeanUtil.toBean(each, TrainingPlanPageRespDTO.class));
    }
}
