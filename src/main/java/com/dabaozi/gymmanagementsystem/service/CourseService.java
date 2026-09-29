package com.dabaozi.gymmanagementsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dabaozi.gymmanagementsystem.pojo.dto.CoursePageReqDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.CourseSaveDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.CourseUpdateDTO;
import com.dabaozi.gymmanagementsystem.pojo.entity.Course;
import com.dabaozi.gymmanagementsystem.pojo.vo.CoursePageRespVO;
import com.dabaozi.gymmanagementsystem.pojo.vo.CourseVO;

/**
 * 课程服务接口
 */
public interface CourseService extends IService<Course> {

    /**
     * 新增课程(课程编号后端自动生成)
     */
    void saveCourse(CourseSaveDTO dto);

    /**
     * 修改课程(课程编号不可修改)
     */
    void updateCourse(CourseUpdateDTO dto);

    /**
     * 删除课程(逻辑删除,有有效报名记录时拒绝)
     */
    void deleteCourse(Long id);

    /**
     * 开课/停课
     */
    void updateCourseStatus(Long id, Integer status);

    /**
     * 课程详情
     */
    CourseVO getCourse(Long id);

    /**
     * 分页查询课程(name/coachName 模糊,type/status 精确)
     */
    CoursePageRespVO pageCourse(CoursePageReqDTO dto);
}
