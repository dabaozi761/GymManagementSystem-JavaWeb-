package com.dabaozi.gymmanagementsystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dabaozi.gymmanagementsystem.common.convention.result.Result;
import com.dabaozi.gymmanagementsystem.common.convention.result.Results;
import com.dabaozi.gymmanagementsystem.pojo.dto.TrainingPlanDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.TrainingPlanPageReqDTO;
import com.dabaozi.gymmanagementsystem.pojo.vo.TrainingPlanPageRespDTO;
import com.dabaozi.gymmanagementsystem.service.TrainingPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class TrainingPlanController {

    private final TrainingPlanService trainingPlanService;

    @PostMapping("api/gym-management-system/trainingPlan/v1/save")
    public Result<Void>save(@RequestBody TrainingPlanDTO trainingPlanDTO){
        trainingPlanService.save(trainingPlanDTO);
        return Results.success();
    }

    @DeleteMapping("api/gym-management-system/trainingPlan/v1/delete")
    public Result<Void>delete(@RequestParam("id") Long id){
        trainingPlanService.deleteWithId(id);
        return Results.success();
    }

    @PostMapping("api/gym-management-system/trainingPlan/v1/update")
    public Result<Void>update(@RequestBody TrainingPlanDTO trainingPlanDTO){
        trainingPlanService.updateTrainingPlan(trainingPlanDTO);
        return Results.success();
    }

    @PostMapping("api/gym-management-system/trainingPlan/v1/page")
    public Result<IPage<TrainingPlanPageRespDTO>>page(@RequestBody TrainingPlanPageReqDTO trainingPlanPageReqDTO){
        return Results.success(trainingPlanService.pageTrainingPlan(trainingPlanPageReqDTO));
    }

}
