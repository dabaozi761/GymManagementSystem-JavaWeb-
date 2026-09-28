package com.dabaozi.gymmanagementsystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dabaozi.gymmanagementsystem.pojo.dto.MemberPageReqDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.MemberSaveDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.MemberUpdateDTO;
import com.dabaozi.gymmanagementsystem.pojo.entity.Member;
import com.dabaozi.gymmanagementsystem.pojo.vo.MemberPageRespVO;
import com.dabaozi.gymmanagementsystem.pojo.vo.MemberVO;

public interface MemberService extends IService<Member> {

    /**
     * 会员新增
     * @param dto
     * @return 会员编号
     */
    String saveMember(MemberSaveDTO dto);

    /**
     * 更新会员
     * @param dto
     */
    void updateMember(MemberUpdateDTO dto);

    /**
     * 删除会员对应的会员卡
     * @param id
     */
    void deleteMember(Long id);

    /**
     * 根据id更新会员的状态
     * @param id
     * @param status
     */
    void updateMemberStatus(Long id, Integer status);

    /**
     * 根据id获取会员详情
     * @param id
     * @return
     */
    MemberVO getMember(Long id);

    /**
     * 分页查询
     * @param dto
     * @return
     */
    MemberPageRespVO pageMember(MemberPageReqDTO dto);
}
