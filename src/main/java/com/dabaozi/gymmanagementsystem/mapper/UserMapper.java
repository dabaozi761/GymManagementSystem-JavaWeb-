package com.dabaozi.gymmanagementsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dabaozi.gymmanagementsystem.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
