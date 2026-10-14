package com.dabaozi.gymmanagementsystem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dabaozi.gymmanagementsystem.common.convention.result.*;
import com.dabaozi.gymmanagementsystem.pojo.dto.CommercePageReqDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.ProductUpdateDTO;
import com.dabaozi.gymmanagementsystem.pojo.entity.Product;
import com.dabaozi.gymmanagementsystem.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/gym-management-system/product/v1")
@RequiredArgsConstructor
public class ProductController {
    private final ShopService shopService;
    @PostMapping("/page")
    public Result<Page<Product>> page(@RequestBody CommercePageReqDTO dto) { return Results.success(shopService.products(dto)); }
    @PostMapping("/save")
    public Result<Void> save(@RequestBody Product dto) { shopService.saveProduct(dto); return Results.success(); }
    @PostMapping("/update")
    public Result<Void> update(@RequestBody ProductUpdateDTO dto) { shopService.updateProduct(dto); return Results.success(); }
    @DeleteMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) { shopService.deleteProduct(id); return Results.success(); }
}
