package com.dabaozi.gymmanagementsystem.pojo.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dabaozi.gymmanagementsystem.pojo.entity.TrainingPlan;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TrainingPlanPageReqDTO extends Page<TrainingPlan> {

    private Long memberId;

    private Long adminId;

    private String title;

    private Integer status;
}
