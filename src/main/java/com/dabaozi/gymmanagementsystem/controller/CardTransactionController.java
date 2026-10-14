package com.dabaozi.gymmanagementsystem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dabaozi.gymmanagementsystem.common.convention.result.*;
import com.dabaozi.gymmanagementsystem.pojo.dto.CommercePageReqDTO;
import com.dabaozi.gymmanagementsystem.pojo.entity.CardTransaction;
import com.dabaozi.gymmanagementsystem.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/gym-management-system/cardTransaction/v1")
@RequiredArgsConstructor
public class CardTransactionController {
    private final WalletService walletService;
    @PostMapping("/page")
    public Result<Page<CardTransaction>> page(@RequestBody CommercePageReqDTO dto) { return Results.success(walletService.page(dto)); }
    @GetMapping("/detail")
    public Result<CardTransaction> detail(@RequestParam Long id) { return Results.success(walletService.detail(id)); }
}
