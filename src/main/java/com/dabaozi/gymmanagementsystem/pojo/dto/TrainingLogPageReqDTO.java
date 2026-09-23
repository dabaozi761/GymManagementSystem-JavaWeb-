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

    private Date trainDate;
}
