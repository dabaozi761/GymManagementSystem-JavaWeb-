package com.dabaozi.gymmanagementsystem.pojo.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dabaozi.gymmanagementsystem.pojo.entity.Equipment;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EquipmentPageReqDTO extends Page<Equipment> {

    private String name;

    private Integer type;

    private Integer status;
}
