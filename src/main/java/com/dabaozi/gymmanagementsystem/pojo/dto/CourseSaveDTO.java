package com.dabaozi.gymmanagementsystem.pojo.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 课程新增请求参数
 */
@Data
public class CourseSaveDTO {

    /**
     * 课程名称
     */
    private String name;

    /**
     * 类型:1 团课 2 私教 3 其他
     */
    private Integer type;

    /**
     * 教练姓名
     */
    private String coachName;

    /**
     * 课程价格
     */
    private BigDecimal price;

    /**
     * 报名人数上限(可空表示不限)
     */
    private Integer capacity;

    /**
     * 状态:1 开课 0 停课(不传默认开课)
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
