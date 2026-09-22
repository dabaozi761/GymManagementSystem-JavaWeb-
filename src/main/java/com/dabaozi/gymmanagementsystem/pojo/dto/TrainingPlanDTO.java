package com.dabaozi.gymmanagementsystem.pojo.dto;

import lombok.Data;

import java.util.Date;

@Data
public class TrainingPlanDTO {

    private Long id;

    /**
     * 会员ID
     */
    private Long memberId;

    /**
     * 制定计划的管理员ID
     */
    private Long adminId;

    /**
     * 计划名称
     */
    private String title;

    /**
     * 训练目标(如:减脂/增肌/塑形)
     */
    private String goal;

    /**
     * 计划内容描述
     */
    private String content;

    /**
     * 计划开始日期
     */
    private Date startDate;

    /**
     * 计划结束日期
     */
    private Date endDate;

    /**
     * 状态:1 进行中 2 已完成 3 已取消
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
