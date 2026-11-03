package com.dabaozi.gymmanagementsystem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dabaozi.gymmanagementsystem.pojo.dto.CommercePageReqDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.ProductUpdateDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.ShopCheckoutDTO;
import com.dabaozi.gymmanagementsystem.pojo.entity.Product;
import com.dabaozi.gymmanagementsystem.pojo.entity.ShopOrder;

/** 商品管理、购买与租用服务。 */
public interface ShopService {
    /** 新增商品。 */
    void saveProduct(Product dto);

    /** 修改商品。 */
    void updateProduct(ProductUpdateDTO dto);

    /** 下架商品，保留历史记录。 */
    void deleteProduct(Long id);

    /** 分页查询商品。 */
    Page<Product> products(CommercePageReqDTO dto);

    /** 使用会员卡余额购买或租用商品。 */
    ShopOrder checkout(ShopCheckoutDTO dto);

    /** 确认租用品归还。 */
    void returnRental(Long id);

    /** 分页查询购物与租用订单。 */
    Page<ShopOrder> orders(CommercePageReqDTO dto);

    /** 查询订单详情。 */
    ShopOrder orderDetail(Long id);
}
