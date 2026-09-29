package com.dabaozi.gymmanagementsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dabaozi.gymmanagementsystem.pojo.dto.CourseEnrollmentPageReqDTO;
import com.dabaozi.gymmanagementsystem.pojo.entity.CourseEnrollment;
import com.dabaozi.gymmanagementsystem.pojo.vo.CourseEnrollmentVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CourseEnrollmentMapper extends BaseMapper<CourseEnrollment> {

    /**
     * 分页查询报名记录(联表带出课程名、会员名)
     */
    IPage<CourseEnrollmentVO> pageCourseEnrollment(CourseEnrollmentPageReqDTO dto);
}
