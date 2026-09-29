package com.dabaozi.gymmanagementsystem.controller;

import com.dabaozi.gymmanagementsystem.common.convention.result.Result;
import com.dabaozi.gymmanagementsystem.common.convention.result.Results;
import com.dabaozi.gymmanagementsystem.pojo.dto.CourseEnrollmentPageReqDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.CourseEnrollmentSaveDTO;
import com.dabaozi.gymmanagementsystem.pojo.vo.CourseEnrollmentPageRespVO;
import com.dabaozi.gymmanagementsystem.pojo.vo.CourseEnrollmentVO;
import com.dabaozi.gymmanagementsystem.service.CourseEnrollmentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 课程报名模块(报名仅会员/普通用户,updateStatus 仅管理员,拦截器校验;数据归属在 service 层校验)
 */
@RestController
@RequestMapping("api/gym-management-system/courseEnrollment/v1")
@RequiredArgsConstructor
public class CourseEnrollmentController {

    private final CourseEnrollmentService courseEnrollmentService;

    /**
     * 报名课程(memberId 从登录态取,不信任前端传参)
     */
    @PostMapping("/enroll")
    public Result<Void> enroll(@RequestBody CourseEnrollmentSaveDTO dto, HttpServletRequest request) {
        courseEnrollmentService.enroll(dto, parseLoginId(request));
        return Results.success();
    }

    /**
     * 取消报名(本人或管理员)
     */
    @PostMapping("/cancel")
    public Result<Void> cancel(@RequestParam Long id, HttpServletRequest request) {
        courseEnrollmentService.cancel(id, parseLoginId(request), parseRole(request));
        return Results.success();
    }

    /**
     * 修改报名状态(仅管理员:2 已取消 3 已完成)
     */
    @PostMapping("/updateStatus")
    public Result<Void> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        courseEnrollmentService.updateStatus(id, status);
        return Results.success();
    }

    /**
     * 报名详情(非管理员只能查看本人记录)
     */
    @GetMapping("/detail")
    public Result<CourseEnrollmentVO> detail(@RequestParam Long id, HttpServletRequest request) {
        return Results.success(courseEnrollmentService.detail(id, parseLoginId(request), parseRole(request)));
    }

    /**
     * 分页查询(非管理员自动只查本人记录)
     */
    @PostMapping("/page")
    public Result<CourseEnrollmentPageRespVO> page(@RequestBody CourseEnrollmentPageReqDTO dto, HttpServletRequest request) {
        return Results.success(courseEnrollmentService.page(dto, parseLoginId(request), parseRole(request)));
    }

    /**
     * 从拦截器放入 request 的属性中取登录id(JWT 载荷数字可能是 Integer/Long,统一转 Long)
     */
    private Long parseLoginId(HttpServletRequest request) {
        Object loginId = request.getAttribute("loginId");
        return loginId == null ? null : Long.valueOf(String.valueOf(loginId));
    }

    /**
     * 取当前登录角色
     */
    private Integer parseRole(HttpServletRequest request) {
        Object role = request.getAttribute("role");
        return role == null ? null : Integer.valueOf(String.valueOf(role));
    }
}
