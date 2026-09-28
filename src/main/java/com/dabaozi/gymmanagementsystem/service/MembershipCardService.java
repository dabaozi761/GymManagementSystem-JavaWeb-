package com.dabaozi.gymmanagementsystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dabaozi.gymmanagementsystem.pojo.dto.*;
import com.dabaozi.gymmanagementsystem.pojo.entity.MembershipCard;
import com.dabaozi.gymmanagementsystem.pojo.vo.MembershipCardPageRespVO;
import com.dabaozi.gymmanagementsystem.pojo.vo.MembershipCardVO;

public interface MembershipCardService extends IService<MembershipCard> {
    /**
     * 保存会员卡号
     * @param membershipCardSaveDTO
     * @return
     */
    String savaCard(MembershipCardSaveDTO membershipCardSaveDTO);

    /**
     * 更新会员卡
     * @param dto
     * @return
     */
    void updateCard(MembershipCardUpdateDTO dto);

    /**
     * 删除会员卡
     * @param id
     */
    void deleteCard(Long id);

    /**
     * 根据id更新会员卡状态
     * @param id
     * @param status
     */
    void updateCardStatus(Long id, Integer status);

    /**
     * 充值接口
     * @param dto
     */
    void recharge(MembershipCardRechargeDTO dto);

    /**
     * 消费
     * @param dto
     */
    void consume(MembershipCardConsumeDTO dto);

    /**
     * 根据id拿到会员卡的详细信息
     * @param id
     * @return
     */
    MembershipCardVO getCard(Long id);

    /**
     * 分页查询
     * @param dto
     * @return
     */
    MembershipCardPageRespVO pageCard(MembershipCardPageReqDTO dto);
}
