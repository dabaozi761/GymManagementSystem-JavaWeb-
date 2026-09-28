package com.dabaozi.gymmanagementsystem.pojo.vo;

import com.dabaozi.gymmanagementsystem.pojo.entity.MembershipCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipCardPageRespVO {
    private long total;//总记录数
    private List<MembershipCardVO> records;//当前页数据集合
    private long current;
    private long size;
    private long pages;//总页数

}
