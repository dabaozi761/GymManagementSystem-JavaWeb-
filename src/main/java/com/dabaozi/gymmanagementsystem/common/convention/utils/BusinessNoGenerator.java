package com.dabaozi.gymmanagementsystem.common.convention.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *会员编号与会员卡编号自动生成.
 */
public final class BusinessNoGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    private BusinessNoGenerator() {
    }

    public static String nextMemberNo() {
        return next("M");
    }

    public static String nextCardNo() {
        return next("C");
    }

    private static String next(String prefix) {
        int sequence = SEQUENCE.updateAndGet(value -> value >= 999 ? 0 : value + 1);
        return prefix + LocalDateTime.now().format(FORMATTER) + String.format("%03d", sequence);
    }
}
