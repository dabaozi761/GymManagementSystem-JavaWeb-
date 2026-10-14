package com.dabaozi.gymmanagementsystem.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.dabaozi.gymmanagementsystem.common.database.BaseDO;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("shop_order")
public class ShopOrder extends BaseDO {
    private String orderNo;
    private String requestNo;
    private Long memberId;
    private String memberName;
    private Long cardId;
    private String cardNo;
    private Long productId;
    private String productName;
    private Integer category;
    private BigDecimal unitPrice;
    private Integer quantity;
    private Integer rentalDays;
    private BigDecimal amount;
    private Integer status; // 1购买完成 2租用中 3已归还
    private LocalDateTime dueTime;
    private LocalDateTime returnTime;
}
