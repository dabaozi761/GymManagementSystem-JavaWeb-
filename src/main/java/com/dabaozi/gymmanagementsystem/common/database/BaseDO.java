package com.dabaozi.gymmanagementsystem.common.database;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;


import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体公共基类:主键、创建/更新时间、逻辑删除标记
 */
@Data
public class BaseDO implements Serializable {

//    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID(数据库自增)
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 创建时间(数据库默认 CURRENT_TIMESTAMP 填充)
     */
    private LocalDateTime createTime;

    /**
     * 更新时间(数据库 ON UPDATE CURRENT_TIMESTAMP 自动更新)
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除:0 未删除 1 已删除
     */
    @TableLogic
    private Integer delFlag;
}
