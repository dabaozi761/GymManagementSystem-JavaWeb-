package com.dabaozi.gymmanagementsystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dabaozi.gymmanagementsystem.pojo.dto.TrainingLogDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.TrainingLogPageReqDTO;
import com.dabaozi.gymmanagementsystem.pojo.entity.TrainingLog;
import com.dabaozi.gymmanagementsystem.pojo.vo.TrainingLogPageRespDTO;


public interface TrainingLogService extends IService<TrainingLog> {
    void save(TrainingLogDTO trainingLogDTO);

    void deleteWithId(Long id);

    void updateTrainingLog(TrainingLogDTO trainingLogDTO);

    IPage<TrainingLogPageRespDTO> pageTrainingLog(TrainingLogPageReqDTO trainingLogPageReqDTO);
}
