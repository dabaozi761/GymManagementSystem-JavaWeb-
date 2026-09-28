package com.dabaozi.gymmanagementsystem.pojo.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dabaozi.gymmanagementsystem.pojo.entity.MembershipCard;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class MembershipCardPageReqDTO  {

    private String cardNo;
    private Long memberId;
    private Integer cardType;
    private Integer status;
    //页码
    private long current=1;
    //每页显示记录数
    private long size=5;
}
