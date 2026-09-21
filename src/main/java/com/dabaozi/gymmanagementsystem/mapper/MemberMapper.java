package com.dabaozi.gymmanagementsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dabaozi.gymmanagementsystem.pojo.entity.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MemberMapper extends BaseMapper<Member> {

    /**
     * 根据用户表的id来查询会员
     * @param id
     * @return
     */
    @Select("select * from member where user_id=#{id}")
    Member selectByUserId(Long id);
}
