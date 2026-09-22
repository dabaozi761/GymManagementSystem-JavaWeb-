package com.dabaozi.gymmanagementsystem.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dabaozi.gymmanagementsystem.common.database.BaseDO;
import lombok.*;

import java.util.Date;

/**
 * 训练日志实体(关联训练计划)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("training_log")
public class TrainingLog extends BaseDO {

    /**
     * 训练计划ID
     */
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
