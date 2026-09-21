package com.dabaozi.gymmanagementsystem.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dabaozi.gymmanagementsystem.common.database.BaseDO;
import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 课程实体(精简版:教练用姓名字段暂存,未建教练表)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("course")
public class Course extends BaseDO {

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
     * 教练姓名(未建教练表,暂存姓名)
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
     * 状态:1 开课 0 停课
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
