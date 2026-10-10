package com.dabaozi.gymmanagementsystem.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingLogPageRespDTO {

    private Long id;

    private Long trainingPlanId;

    /**
     * 训练计划名称(查询后补全,便于会员侧展示)
     */
    private String planTitle;

    private Date trainDate;

    private Integer durationMinutes;

    private String content;

    private String feeling;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
