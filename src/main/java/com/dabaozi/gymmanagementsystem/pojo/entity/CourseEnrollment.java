package com.dabaozi.gymmanagementsystem.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dabaozi.gymmanagementsystem.common.database.BaseDO;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDate;

/**
 * 课程报名实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("course_enrollment")
public class CourseEnrollment extends BaseDO {

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 报名会员ID
     */
    private Long memberId;

    /**
     * 报名日期(数据库默认当天)
     */
    private LocalDate enrollDate;

    /**
     * 状态:1 已报名 2 已取消 3 已完成
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
