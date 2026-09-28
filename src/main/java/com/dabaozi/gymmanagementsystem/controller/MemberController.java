package com.dabaozi.gymmanagementsystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dabaozi.gymmanagementsystem.common.convention.result.Result;
import com.dabaozi.gymmanagementsystem.common.convention.result.Results;
import com.dabaozi.gymmanagementsystem.pojo.dto.MemberPageReqDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.MemberSaveDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.MemberUpdateDTO;
import com.dabaozi.gymmanagementsystem.pojo.vo.MemberPageRespVO;
import com.dabaozi.gymmanagementsystem.pojo.vo.MemberVO;
import com.dabaozi.gymmanagementsystem.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/gym-management-system/member/v1")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/save")
    public Result<String> save(@RequestBody MemberSaveDTO dto) {
        return Results.success(memberService.saveMember(dto));
    }

    @PostMapping("/update")
    public Result<Void> update(@RequestBody MemberUpdateDTO dto) {
        memberService.updateMember(dto);
        return Results.success();
    }

    @DeleteMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        memberService.deleteMember(id);
        return Results.success();
    }

    @PostMapping("/updateStatus")
    public Result<Void> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        memberService.updateMemberStatus(id, status);
        return Results.success();
    }

    @GetMapping("/detail")
    public Result<MemberVO> detail(@RequestParam Long id) {
        return Results.success(memberService.getMember(id));
    }

    @PostMapping("/page")
    public Result<MemberPageRespVO> page(@RequestBody MemberPageReqDTO dto) {
        return Results.success(memberService.pageMember(dto));
    }
}
