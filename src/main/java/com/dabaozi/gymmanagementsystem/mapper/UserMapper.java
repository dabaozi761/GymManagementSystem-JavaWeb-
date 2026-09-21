package com.dabaozi.gymmanagementsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dabaozi.gymmanagementsystem.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据username查询普通用户
     * @param username
     * @return
     */
    @Select("select * from user where username=#{username}")
    User selectByUsername(String username);
}
