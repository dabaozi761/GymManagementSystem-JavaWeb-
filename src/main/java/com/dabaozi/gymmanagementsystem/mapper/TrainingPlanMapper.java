package com.dabaozi.gymmanagementsystem.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dabaozi.gymmanagementsystem.pojo.dto.TrainingPlanPageReqDTO;
import com.dabaozi.gymmanagementsystem.pojo.entity.TrainingPlan;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TrainingPlanMapper extends BaseMapper<TrainingPlan> {

    IPage<TrainingPlan> pageTrainingPlan(TrainingPlanPageReqDTO trainingPlanPageReqDTO);
}
