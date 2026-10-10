package com.dabaozi.gymmanagementsystem.pojo.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dabaozi.gymmanagementsystem.pojo.entity.TrainingLog;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class TrainingLogPageReqDTO extends Page<TrainingLog> {

    private Long trainingPlanId;

    /**
     * 会员id(非管理员强制按本人过滤,经训练计划关联)
     */
    private Long memberId;

    private Date trainDate;
}
