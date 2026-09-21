package com.dabaozi.gymmanagementsystem.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dabaozi.gymmanagementsystem.common.database.BaseDO;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDate;

/**
 * 训练计划实体(管理员为会员制定)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("training_plan")
public class TrainingPlan extends BaseDO {

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
    private LocalDate startDate;

    /**
     * 计划结束日期
     */
    private LocalDate endDate;

    /**
     * 状态:1 进行中 2 已完成 3 已取消
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
