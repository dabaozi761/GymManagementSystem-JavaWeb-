package com.dabaozi.gymmanagementsystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dabaozi.gymmanagementsystem.common.convention.result.Result;
import com.dabaozi.gymmanagementsystem.common.convention.result.Results;
import com.dabaozi.gymmanagementsystem.pojo.dto.*;
import com.dabaozi.gymmanagementsystem.pojo.entity.MembershipCard;
import com.dabaozi.gymmanagementsystem.pojo.vo.MembershipCardPageRespVO;
import com.dabaozi.gymmanagementsystem.pojo.vo.MembershipCardVO;
import com.dabaozi.gymmanagementsystem.service.MembershipCardService;
//import com.sun.org.apache.xerces.internal.parsers.DTDParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/gym-management-system/membershipCard/vi")
@Slf4j
@RequiredArgsConstructor

public class MembershipCardController {
    private final MembershipCardService membershipCardService;

    @PostMapping("/save")
    public Result<String> save(@RequestBody MembershipCardSaveDTO membershipCardSaveDTO){
        log.info("新增会员卡：{}",membershipCardSaveDTO);
        return Results.success(membershipCardService.savaCard(membershipCardSaveDTO));
    }

    @PostMapping("/update")
    public Result<Void> update(@RequestBody MembershipCardUpdateDTO dto){
        log.info("更新会员卡：{}",dto);
        membershipCardService.updateCard(dto);
        return Results.success();
    }

    @DeleteMapping("/delete")
    public Result<Void> delete(@RequestParam Long id){
        log.info("删除会员卡，id为{}",id);
        membershipCardService.deleteCard(id);
        return Results.success();
    }
    @PostMapping("/updateStatus")
    public Result<Void> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        membershipCardService.updateCardStatus(id, status);
        return Results.success();
    }

    @PostMapping("/recharge")
    public Result<Void> recharge(@RequestBody MembershipCardRechargeDTO dto) {
        membershipCardService.recharge(dto);
        return Results.success();
    }

    @PostMapping("/consume")
    public Result<Void> consume(@RequestBody MembershipCardConsumeDTO dto) {
        membershipCardService.consume(dto);
        return Results.success();
    }

    @GetMapping("/detail")
    public Result<MembershipCardVO> detail(@RequestParam Long id) {
        return Results.success(membershipCardService.getCard(id));
    }

    @PostMapping("/page")
    public Result<MembershipCardPageRespVO> page(@RequestBody MembershipCardPageReqDTO dto) {
        return Results.success(membershipCardService.pageCard(dto));
    }




}
