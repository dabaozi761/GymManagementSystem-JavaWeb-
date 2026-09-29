package com.dabaozi.gymmanagementsystem.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 课程分页查询请求参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CoursePageReqDTO {

    /**
     * 课程名称(模糊)
     */
    private String name;

    /**
     * 类型:1 团课 2 私教 3 其他
     */
    private Integer type;

    /**
     * 状态:1 开课 0 停课
     */
    private Integer status;

    /**
     * 教练姓名(模糊)
     */
    private String coachName;

    /**
     * 页码
     */
    private long current = 1;

    /**
     * 每页显示记录数
     */
    private long size = 5;
}
