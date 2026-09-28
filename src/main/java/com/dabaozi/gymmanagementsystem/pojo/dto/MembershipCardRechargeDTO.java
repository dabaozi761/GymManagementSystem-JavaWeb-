package com.dabaozi.gymmanagementsystem.pojo.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MembershipCardRechargeDTO {
    private Long id;
    private BigDecimal amount;
}
