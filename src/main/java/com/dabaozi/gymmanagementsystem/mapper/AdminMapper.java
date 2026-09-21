package com.dabaozi.gymmanagementsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dabaozi.gymmanagementsystem.pojo.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminMapper extends BaseMapper<Admin> {
    /**
     * 根据username查询管理员
     * @param username
     * @return
     */
    @Select("select * from admin where username=#{username}")
    Admin selectByUsername(String username);
}
