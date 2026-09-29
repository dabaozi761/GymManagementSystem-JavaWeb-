package com.dabaozi.gymmanagementsystem.controller;

import com.dabaozi.gymmanagementsystem.common.convention.result.Result;
import com.dabaozi.gymmanagementsystem.common.convention.result.Results;
import com.dabaozi.gymmanagementsystem.pojo.dto.CoursePageReqDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.CourseSaveDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.CourseUpdateDTO;
import com.dabaozi.gymmanagementsystem.pojo.vo.CoursePageRespVO;
import com.dabaozi.gymmanagementsystem.pojo.vo.CourseVO;
import com.dabaozi.gymmanagementsystem.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 课程管理模块(写接口仅管理员,拦截器校验)
 */
@RestController
@RequestMapping("api/gym-management-system/course/v1")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /**
     * 新增课程
     */
    @PostMapping("/save")
    public Result<Void> save(@RequestBody CourseSaveDTO dto) {
        courseService.saveCourse(dto);
        return Results.success();
    }

    /**
     * 修改课程
     */
    @PostMapping("/update")
    public Result<Void> update(@RequestBody CourseUpdateDTO dto) {
        courseService.updateCourse(dto);
        return Results.success();
    }

    /**
     * 删除课程(逻辑删除)
     */
    @DeleteMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) {
        courseService.deleteCourse(id);
        return Results.success();
    }

    /**
     * 开课/停课
     */
    @PostMapping("/updateStatus")
    public Result<Void> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        courseService.updateCourseStatus(id, status);
        return Results.success();
    }

    /**
     * 课程详情
     */
    @GetMapping("/detail")
    public Result<CourseVO> detail(@RequestParam Long id) {
        return Results.success(courseService.getCourse(id));
    }

    /**
     * 分页查询课程
     */
    @PostMapping("/page")
    public Result<CoursePageRespVO> page(@RequestBody CoursePageReqDTO dto) {
        return Results.success(courseService.pageCourse(dto));
    }
}
