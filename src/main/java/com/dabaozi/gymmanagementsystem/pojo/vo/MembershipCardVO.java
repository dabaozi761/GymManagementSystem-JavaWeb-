package com.dabaozi.gymmanagementsystem.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MembershipCardVO {

    private Long id;
    private String cardNo;
    private Long memberId;
    private String memberNo;
    private String memberName;
    private Integer cardType;
    private BigDecimal balance;
    private Integer totalCount;
    private Integer remainCount;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
