package com.dabaozi.gymmanagementsystem.pojo.dto;

import lombok.Data;

@Data
public class ShopCheckoutDTO {
    private Long productId;
    private Long cardId;
    private Integer quantity;
    private Integer rentalDays;
    private String requestNo; // 同次提交/网络重试保持相同，防止重复扣款。
}
