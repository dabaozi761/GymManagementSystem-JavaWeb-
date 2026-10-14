package com.dabaozi.gymmanagementsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dabaozi.gymmanagementsystem.pojo.entity.ShopOrder;
import org.apache.ibatis.annotations.*;

@Mapper
public interface ShopOrderMapper extends BaseMapper<ShopOrder> {
    @Select("SELECT * FROM shop_order WHERE id=#{id} AND del_flag=0 FOR UPDATE")
    ShopOrder selectForUpdate(Long id);
    @Select("SELECT * FROM shop_order WHERE request_no=#{requestNo} AND del_flag=0")
    ShopOrder selectByRequestNo(String requestNo);
}
