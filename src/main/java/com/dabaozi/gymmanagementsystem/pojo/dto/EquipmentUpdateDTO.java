package com.dabaozi.gymmanagementsystem.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EquipmentUpdateDTO {
    private String equipmentNo;
    private String name;
    private Integer type;
    private Integer status;
    private Date purchaseDate;
    private BigDecimal purchasePrice;
    private String remark;
}
