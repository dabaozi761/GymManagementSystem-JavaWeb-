package com.dabaozi.gymmanagementsystem.pojo.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MembershipCardUpdateDTO {

    private Long id;
    private Long memberId;
    private Integer cardType;
    private BigDecimal balance;
    private Integer totalCount;
    private Integer remainCount;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status;
}
