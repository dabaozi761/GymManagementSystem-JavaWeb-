package com.dabaozi.gymmanagementsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dabaozi.gymmanagementsystem.pojo.dto.CourseEnrollmentPageReqDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.CourseEnrollmentSaveDTO;
import com.dabaozi.gymmanagementsystem.pojo.entity.CourseEnrollment;
import com.dabaozi.gymmanagementsystem.pojo.vo.CourseEnrollmentPageRespVO;
import com.dabaozi.gymmanagementsystem.pojo.vo.CourseEnrollmentVO;

/**
 * 课程报名服务接口
 */
public interface CourseEnrollmentService extends IService<CourseEnrollment> {

    /**
     * 报名课程(memberId 从登录态 loginId 取;课程须开课、会员须在籍、不重复、不超容量)
     */
    void enroll(CourseEnrollmentSaveDTO dto, Long loginId);

    /**
     * 取消报名(本人或管理员)
     */
    void cancel(Long id, Long loginId, Integer role);

    /**
     * 修改报名状态(仅管理员:2 已取消 3 已完成)
     */
    void updateStatus(Long id, Integer status);

    /**
     * 报名详情(非管理员只能查看本人记录)
     */
    CourseEnrollmentVO detail(Long id, Long loginId, Integer role);

    /**
     * 分页查询(非管理员自动只查本人记录)
     */
    CourseEnrollmentPageRespVO page(CourseEnrollmentPageReqDTO dto, Long loginId, Integer role);
}
