package com.dabaozi.gymmanagementsystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dabaozi.gymmanagementsystem.pojo.dto.TrainingLogDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.TrainingLogPageReqDTO;
import com.dabaozi.gymmanagementsystem.pojo.vo.TrainingLogPageRespDTO;


public interface TrainingLogService {
    void save(TrainingLogDTO trainingLogDTO);

    void deleteWithId(Long id);

    void updateTrainingLog(TrainingLogDTO trainingLogDTO);

    IPage<TrainingLogPageRespDTO> pageTrainingLog(TrainingLogPageReqDTO trainingLogPageReqDTO);
}
