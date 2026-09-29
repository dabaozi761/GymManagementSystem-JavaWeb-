package com.dabaozi.gymmanagementsystem.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 课程报名返回参数(课程名、会员名由联表查出)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseEnrollmentVO {

    private Long id;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程名称(联表)
     */
    private String courseName;

    /**
     * 报名会员ID
     */
    private Long memberId;

    /**
     * 会员姓名(联表)
     */
    private String memberName;

    /**
     * 报名日期
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

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
