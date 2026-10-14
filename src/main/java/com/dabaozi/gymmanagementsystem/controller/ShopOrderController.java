package com.dabaozi.gymmanagementsystem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dabaozi.gymmanagementsystem.common.convention.result.*;
import com.dabaozi.gymmanagementsystem.pojo.dto.*;
import com.dabaozi.gymmanagementsystem.pojo.entity.ShopOrder;
import com.dabaozi.gymmanagementsystem.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/gym-management-system/shopOrder/v1")
@RequiredArgsConstructor
public class ShopOrderController {
    private final ShopService shopService;
    @PostMapping("/checkout")
    public Result<ShopOrder> checkout(@RequestBody ShopCheckoutDTO dto) { return Results.success(shopService.checkout(dto)); }
    @PostMapping("/returnRental")
    public Result<Void> returnRental(@RequestParam Long id) { shopService.returnRental(id); return Results.success(); }
    @PostMapping("/page")
    public Result<Page<ShopOrder>> page(@RequestBody CommercePageReqDTO dto) { return Results.success(shopService.orders(dto)); }
    @GetMapping("/detail")
    public Result<ShopOrder> detail(@RequestParam Long id) { return Results.success(shopService.orderDetail(id)); }
}
