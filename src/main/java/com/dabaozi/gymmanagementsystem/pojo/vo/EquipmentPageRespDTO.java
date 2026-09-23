package com.dabaozi.gymmanagementsystem.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentPageRespDTO {

    private Long id;

    private String equipmentNo;

    private String name;

    private Integer type;

    private Integer status;

    private Date purchaseDate;

    private BigDecimal purchasePrice;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
