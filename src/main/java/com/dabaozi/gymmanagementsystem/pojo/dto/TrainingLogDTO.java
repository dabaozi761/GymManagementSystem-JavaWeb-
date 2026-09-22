package com.dabaozi.gymmanagementsystem.pojo.dto;

import lombok.Data;

import java.util.Date;

@Data
public class TrainingLogDTO {

    private Long id;

    private Long trainingPlanId;

    /**
     * 训练日期(数据库默认当天)
     */
    private Date trainDate;

    /**
     * 训练时长(分钟)
     */
    private Integer durationMinutes;

    /**
     * 训练内容记录
     */
    private String content;

    /**
     * 身体感受
     */
    private String feeling;

    /**
     * 备注
     */
    private String remark;
}
