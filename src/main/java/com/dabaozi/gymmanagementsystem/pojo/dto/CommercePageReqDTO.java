package com.dabaozi.gymmanagementsystem.pojo.dto;

import com.dabaozi.gymmanagementsystem.common.convention.exception.ClientException;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommercePageReqDTO {
    private long current = 1;
    private long size = 10;
    private Long memberId;
    private Long cardId;
    private Integer type;
    private Integer category;
    private Integer status;
    private String name;
    private LocalDateTime fromTime;
    private LocalDateTime toTime;

    public void validate() {
        if (current < 1 || size < 1 || size > 100) throw new ClientException("分页参数不合法，每页最多100条");
        if (fromTime != null && toTime != null && fromTime.isAfter(toTime)) throw new ClientException("开始时间不能晚于结束时间");
    }
}
