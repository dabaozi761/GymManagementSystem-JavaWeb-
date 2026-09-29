package com.dabaozi.gymmanagementsystem.pojo.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dabaozi.gymmanagementsystem.pojo.entity.CourseEnrollment;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程报名分页查询请求参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CourseEnrollmentPageReqDTO extends Page<CourseEnrollment> {

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 会员ID
     */
    private Long memberId;

    /**
     * 状态:1 已报名 2 已取消 3 已完成
     */
    private Integer status;
}
