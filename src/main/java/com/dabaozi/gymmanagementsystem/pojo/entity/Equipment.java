package com.dabaozi.gymmanagementsystem.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dabaozi.gymmanagementsystem.common.database.BaseDO;
import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 器材实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("equipment")
public class Equipment extends BaseDO {

    /**
     * 器材编号(唯一)
     */
    private String equipmentNo;

    /**
     * 器材名称(如:跑步机)
     */
    private String name;

    /**
     * 类型:1 有氧 2 力量 3 自由重量 4 其他
     */
    private Integer type;

    /**
     * 状态:1 可用 2 维修中 3 报废
     */
    private Integer status;

    /**
     * 购入日期
     */
    private Date purchaseDate;

    /**
     * 购入价格
     */
    private BigDecimal purchasePrice;

    /**
     * 备注
     */
    private String remark;
}
