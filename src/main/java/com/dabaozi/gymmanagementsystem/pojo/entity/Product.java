package com.dabaozi.gymmanagementsystem.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dabaozi.gymmanagementsystem.common.database.BaseDO;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("shop_product")
public class Product extends BaseDO {
    private String name;
    private Integer category; // 1储物柜 2充电宝 3饮料 4蛋白粉 5运动周边
    private BigDecimal price; // 租用品按件/天，其余按件
    private Integer stock; // 可购买/可出租库存
    private Integer status; // 1上架 0下架
    private String remark;
}
