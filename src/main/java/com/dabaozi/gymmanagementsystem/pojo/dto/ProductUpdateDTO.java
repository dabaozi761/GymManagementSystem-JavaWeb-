package com.dabaozi.gymmanagementsystem.pojo.dto;

import com.dabaozi.gymmanagementsystem.pojo.entity.Product;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 编辑时携带看到的旧库存，避免覆盖编辑期间发生的购买或归还。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductUpdateDTO extends Product {
    private Integer expectedStock;
}
