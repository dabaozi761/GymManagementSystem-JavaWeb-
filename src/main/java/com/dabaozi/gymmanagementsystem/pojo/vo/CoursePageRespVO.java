package com.dabaozi.gymmanagementsystem.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 课程分页查询返回参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoursePageRespVO {

    /**
     * 总记录数
     */
    private long total;

    /**
     * 当前页数据集合
     */
    private List<CourseVO> records;

    private long current;

    private long size;

    /**
     * 总页数
     */
    private long pages;
}
