package com.dabaozi.gymmanagementsystem.pojo.dto;

import lombok.Data;

/**
 * 课程报名请求参数(memberId 从登录态取,不信任前端传参)
 */
@Data
public class CourseEnrollmentSaveDTO {

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 备注
     */
    private String remark;
}
