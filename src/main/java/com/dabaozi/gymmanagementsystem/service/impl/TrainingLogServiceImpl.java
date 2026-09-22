package com.dabaozi.gymmanagementsystem.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dabaozi.gymmanagementsystem.mapper.TrainingLogMapper;
import com.dabaozi.gymmanagementsystem.pojo.dto.TrainingLogDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.TrainingLogPageReqDTO;
import com.dabaozi.gymmanagementsystem.pojo.entity.TrainingLog;
import com.dabaozi.gymmanagementsystem.pojo.vo.TrainingLogPageRespDTO;
import com.dabaozi.gymmanagementsystem.service.TrainingLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrainingLogServiceImpl extends ServiceImpl<TrainingLogMapper,TrainingLog> implements TrainingLogService {



    @Override
    public void save(TrainingLogDTO trainingLogDTO) {
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
    public void deleteWithId(Long id) {
        LambdaUpdateWrapper<TrainingLog> updateWrapper = Wrappers.lambdaUpdate(TrainingLog.class)
                .eq(TrainingLog::getId, id)
                .eq(TrainingLog::getDelFlag, 0);
        baseMapper.delete(updateWrapper);
    }

    @Override
    public void updateTrainingLog(TrainingLogDTO trainingLogDTO) {
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
    public IPage<TrainingLogPageRespDTO> pageTrainingLog(TrainingLogPageReqDTO trainingLogPageReqDTO) {
        IPage<TrainingLog> pageResult = baseMapper.pageTrainingLog(trainingLogPageReqDTO);
        return pageResult.convert(each -> BeanUtil.toBean(each, TrainingLogPageRespDTO.class));
    }
}
