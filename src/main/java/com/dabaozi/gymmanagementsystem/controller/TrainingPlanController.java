package com.dabaozi.gymmanagementsystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dabaozi.gymmanagementsystem.common.convention.result.Result;
import com.dabaozi.gymmanagementsystem.common.convention.result.Results;
import com.dabaozi.gymmanagementsystem.pojo.dto.TrainingPlanDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.TrainingPlanPageReqDTO;
import com.dabaozi.gymmanagementsystem.pojo.vo.TrainingPlanPageRespDTO;
import com.dabaozi.gymmanagementsystem.service.TrainingPlanService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 训练计划模块(非管理员只能操作/查询本人的计划,数据归属在 service 层校验)
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
public class TrainingPlanController {

    private final TrainingPlanService trainingPlanService;

    @PostMapping("api/gym-management-system/trainingPlan/v1/save")
    public Result<Void>save(@RequestBody TrainingPlanDTO trainingPlanDTO, HttpServletRequest request){
        trainingPlanService.save(trainingPlanDTO, parseLoginId(request), parseRole(request));
        return Results.success();
    }

    @DeleteMapping("api/gym-management-system/trainingPlan/v1/delete")
    public Result<Void>delete(@RequestParam("id") Long id, HttpServletRequest request){
        trainingPlanService.deleteWithId(id, parseLoginId(request), parseRole(request));
        return Results.success();
    }

    @PostMapping("api/gym-management-system/trainingPlan/v1/update")
    public Result<Void>update(@RequestBody TrainingPlanDTO trainingPlanDTO, HttpServletRequest request){
        trainingPlanService.updateTrainingPlan(trainingPlanDTO, parseLoginId(request), parseRole(request));
        return Results.success();
    }

    @PostMapping("api/gym-management-system/trainingPlan/v1/page")
    public Result<IPage<TrainingPlanPageRespDTO>>page(@RequestBody TrainingPlanPageReqDTO trainingPlanPageReqDTO, HttpServletRequest request){
        return Results.success(trainingPlanService.pageTrainingPlan(trainingPlanPageReqDTO, parseLoginId(request), parseRole(request)));
    }

    /**
     * 从拦截器放入 request 的属性中取登录id(JWT 载荷数字可能是 Integer/Long,统一转 Long)
     */
    private Long parseLoginId(HttpServletRequest request) {
        Object loginId = request.getAttribute("loginId");
        return loginId == null ? null : Long.valueOf(String.valueOf(loginId));
    }

    /**
     * 取当前登录角色
     */
    private Integer parseRole(HttpServletRequest request) {
        Object role = request.getAttribute("role");
        return role == null ? null : Integer.valueOf(String.valueOf(role));
    }

}
