package com.dabaozi.gymmanagementsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dabaozi.gymmanagementsystem.pojo.entity.MembershipCard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MembershipCardMapper extends BaseMapper<MembershipCard> {
    @Select("SELECT * FROM membership_card WHERE id=#{id} AND del_flag=0 FOR UPDATE")
    MembershipCard selectForUpdate(Long id);
}
