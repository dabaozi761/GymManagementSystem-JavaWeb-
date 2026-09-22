package com.dabaozi.gymmanagementsystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dabaozi.gymmanagementsystem.common.convention.result.Result;
import com.dabaozi.gymmanagementsystem.common.convention.result.Results;
import com.dabaozi.gymmanagementsystem.pojo.dto.TrainingLogDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.TrainingLogPageReqDTO;
import com.dabaozi.gymmanagementsystem.pojo.vo.TrainingLogPageRespDTO;
import com.dabaozi.gymmanagementsystem.service.TrainingLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class TrainingLogController {

    private final TrainingLogService trainingLogService;

    @PostMapping("api/gym-management-system/trainingLog/v1/save")
    public Result<Void>save(@RequestBody TrainingLogDTO trainingLogDTO){
        trainingLogService.save(trainingLogDTO);
        return Results.success();
    }

    @DeleteMapping("api/gym-management-system/trainingLog/v1/delete")
    public Result<Void>delete(@RequestParam("id") Long id){
        trainingLogService.deleteWithId(id);
        return Results.success();
    }

    @PostMapping("api/gym-management-system/trainingLog/v1/update")
    public Result<Void>update(@RequestBody TrainingLogDTO trainingLogDTO){
        trainingLogService.updateTrainingLog(trainingLogDTO);
        return Results.success();
    }

    @PostMapping("api/gym-management-system/trainingLog/v1/page")
    public Result<IPage<TrainingLogPageRespDTO>>page(@RequestBody TrainingLogPageReqDTO trainingLogPageReqDTO){
        return Results.success(trainingLogService.pageTrainingLog(trainingLogPageReqDTO));
    }
}
