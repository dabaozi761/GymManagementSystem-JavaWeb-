package com.dabaozi.gymmanagementsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dabaozi.gymmanagementsystem.pojo.entity.Product;
import org.apache.ibatis.annotations.*;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    @Select("SELECT * FROM shop_product WHERE id=#{id} AND del_flag=0 FOR UPDATE")
    Product selectForUpdate(Long id);
}
