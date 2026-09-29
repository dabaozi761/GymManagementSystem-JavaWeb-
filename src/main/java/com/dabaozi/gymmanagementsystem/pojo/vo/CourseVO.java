package com.dabaozi.gymmanagementsystem.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程详情返回参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseVO {

    private Long id;

    /**
     * 课程编号(唯一)
     */
    private String courseNo;

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
     * 报名人数上限(空表示不限)
     */
    private Integer capacity;

    /**
     * 状态:1 开课 0 停课
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
