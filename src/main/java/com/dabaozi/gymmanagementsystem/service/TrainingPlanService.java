package com.dabaozi.gymmanagementsystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dabaozi.gymmanagementsystem.pojo.dto.TrainingPlanDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.TrainingPlanPageReqDTO;
import com.dabaozi.gymmanagementsystem.pojo.entity.TrainingPlan;
import com.dabaozi.gymmanagementsystem.pojo.vo.TrainingPlanPageRespDTO;

public interface TrainingPlanService extends IService<TrainingPlan> {
    void save(TrainingPlanDTO trainingPlanDTO);

    void deleteWithId(Long id);

    void updateTrainingPlan(TrainingPlanDTO trainingPlanDTO);

    IPage<TrainingPlanPageRespDTO> pageTrainingPlan(TrainingPlanPageReqDTO trainingPlanPageReqDTO);
}
