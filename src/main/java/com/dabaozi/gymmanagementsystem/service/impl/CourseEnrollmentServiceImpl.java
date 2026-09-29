package com.dabaozi.gymmanagementsystem.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dabaozi.gymmanagementsystem.common.convention.exception.ClientException;
import com.dabaozi.gymmanagementsystem.mapper.CourseEnrollmentMapper;
import com.dabaozi.gymmanagementsystem.mapper.CourseMapper;
import com.dabaozi.gymmanagementsystem.mapper.MemberMapper;
import com.dabaozi.gymmanagementsystem.pojo.dto.CourseEnrollmentPageReqDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.CourseEnrollmentSaveDTO;
import com.dabaozi.gymmanagementsystem.pojo.entity.Course;
import com.dabaozi.gymmanagementsystem.pojo.entity.CourseEnrollment;
import com.dabaozi.gymmanagementsystem.pojo.entity.Member;
import com.dabaozi.gymmanagementsystem.pojo.vo.CourseEnrollmentPageRespVO;
import com.dabaozi.gymmanagementsystem.pojo.vo.CourseEnrollmentVO;
import com.dabaozi.gymmanagementsystem.service.CourseEnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 课程报名服务实现
 */
@Service
@RequiredArgsConstructor
public class CourseEnrollmentServiceImpl extends ServiceImpl<CourseEnrollmentMapper, CourseEnrollment> implements CourseEnrollmentService {

    /**
     * 课程状态:开课
     */
    private static final int COURSE_OPEN = 1;

    /**
     * 会员状态:在籍
     */
    private static final int MEMBER_NORMAL = 1;

    /**
     * 报名状态:已报名
     */
    private static final int STATUS_ENROLLED = 1;

    /**
     * 报名状态:已取消
     */
    private static final int STATUS_CANCELED = 2;

    /**
     * 报名状态:已完成
     */
    private static final int STATUS_FINISHED = 3;

    /**
     * 角色:管理员
     */
    private static final int ROLE_ADMIN = 1;

    private final CourseMapper courseMapper;

    private final MemberMapper memberMapper;

    @Override
    public void enroll(CourseEnrollmentSaveDTO dto, Long loginId) {
        if (dto == null || dto.getCourseId() == null) {
            throw new ClientException("课程id不能为空");
        }
        Course course = courseMapper.selectById(dto.getCourseId());
        if (course == null) {
            throw new ClientException("课程不存在或已删除");
        }
        if (!Integer.valueOf(COURSE_OPEN).equals(course.getStatus())) {
            throw new ClientException("课程未开课，无法报名");
        }
        Member member = requireMemberByLoginId(loginId);
        // 防重复报名:同一课程同一会员已报名的记录存在则拒绝
        Long enrolledCount = baseMapper.selectCount(Wrappers.lambdaQuery(CourseEnrollment.class)
                .eq(CourseEnrollment::getCourseId, dto.getCourseId())
                .eq(CourseEnrollment::getMemberId, member.getId())
                .eq(CourseEnrollment::getStatus, STATUS_ENROLLED));
        if (enrolledCount != null && enrolledCount > 0) {
            throw new ClientException("您已报名该课程，请勿重复报名");
        }
        // 容量校验:报名人数上限为空表示不限
        if (course.getCapacity() != null) {
            Long totalEnrolled = baseMapper.selectCount(Wrappers.lambdaQuery(CourseEnrollment.class)
                    .eq(CourseEnrollment::getCourseId, dto.getCourseId())
                    .eq(CourseEnrollment::getStatus, STATUS_ENROLLED));
            if (totalEnrolled != null && totalEnrolled >= course.getCapacity()) {
                throw new ClientException("课程报名人数已满");
            }
        }
        CourseEnrollment enrollment = CourseEnrollment.builder()
                .courseId(dto.getCourseId())
                .memberId(member.getId())
                .status(STATUS_ENROLLED)
                .remark(dto.getRemark())
                .build();
        baseMapper.insert(enrollment);
    }

    @Override
    public void cancel(Long id, Long loginId, Integer role) {
        CourseEnrollment enrollment = requireEnrollment(id);
        // 非管理员只能取消自己的报名
        if (role == null || role != ROLE_ADMIN) {
            Member member = requireMemberByLoginId(loginId);
            if (!member.getId().equals(enrollment.getMemberId())) {
                throw new ClientException("无权操作他人的报名记录");
            }
        }
        if (!Integer.valueOf(STATUS_ENROLLED).equals(enrollment.getStatus())) {
            throw new ClientException("只有已报名状态才能取消");
        }
        updateEnrollmentStatus(id, STATUS_CANCELED);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != STATUS_CANCELED && status != STATUS_FINISHED)) {
            throw new ClientException("报名状态只能是2(已取消)或3(已完成)");
        }
        requireEnrollment(id);
        updateEnrollmentStatus(id, status);
    }

    @Override
    public CourseEnrollmentVO detail(Long id, Long loginId, Integer role) {
        CourseEnrollment enrollment = requireEnrollment(id);
        // 非管理员只能查看自己的报名记录
        if (role == null || role != ROLE_ADMIN) {
            Member member = requireMemberByLoginId(loginId);
            if (!member.getId().equals(enrollment.getMemberId())) {
                throw new ClientException("无权查看他人的报名记录");
            }
        }
        return toVO(enrollment);
    }

    @Override
    public CourseEnrollmentPageRespVO page(CourseEnrollmentPageReqDTO dto, Long loginId, Integer role) {
        if (dto == null) {
            dto = new CourseEnrollmentPageReqDTO();
        }
        // 非管理员强制只查本人
        if (role == null || role != ROLE_ADMIN) {
            Member member = memberMapper.selectByUserId(loginId);
            if (member == null) {
                return CourseEnrollmentPageRespVO.builder()
                        .total(0)
                        .records(Collections.emptyList())
                        .current(dto.getCurrent())
                        .size(dto.getSize())
                        .pages(0)
                        .build();
            }
            dto.setMemberId(member.getId());
        }
        IPage<CourseEnrollmentVO> pageResult = baseMapper.pageCourseEnrollment(dto);
        return CourseEnrollmentPageRespVO.builder()
                .total(pageResult.getTotal())
                .records(pageResult.getRecords())
                .current(pageResult.getCurrent())
                .size(pageResult.getSize())
                .pages(pageResult.getPages())
                .build();
    }

    /**
     * 根据登录态用户id查找会员,查不到则提示先办理会员
     */
    private Member requireMemberByLoginId(Long loginId) {
        if (loginId == null) {
            throw new ClientException("登录信息缺失");
        }
        Member member = memberMapper.selectByUserId(loginId);
        if (member == null) {
            throw new ClientException("当前账号未关联会员，请先办理会员");
        }
        if (!Integer.valueOf(MEMBER_NORMAL).equals(member.getStatus())) {
            throw new ClientException("会员已退会，无法操作");
        }
        return member;
    }

    private CourseEnrollment requireEnrollment(Long id) {
        if (id == null) {
            throw new ClientException("报名记录id不能为空");
        }
        CourseEnrollment enrollment = baseMapper.selectById(id);
        if (enrollment == null) {
            throw new ClientException("报名记录不存在或已删除");
        }
        return enrollment;
    }

    private void updateEnrollmentStatus(Long id, Integer status) {
        baseMapper.update(null, Wrappers.lambdaUpdate(CourseEnrollment.class)
                .eq(CourseEnrollment::getId, id)
                .set(CourseEnrollment::getStatus, status));
    }

    /**
     * 报名记录转 VO(补课程名、会员名)
     */
    private CourseEnrollmentVO toVO(CourseEnrollment enrollment) {
        CourseEnrollmentVO vo = BeanUtil.toBean(enrollment, CourseEnrollmentVO.class);
        Course course = courseMapper.selectById(enrollment.getCourseId());
        if (course != null) {
            vo.setCourseName(course.getName());
        }
        Member member = memberMapper.selectById(enrollment.getMemberId());
        if (member != null) {
            vo.setMemberName(member.getName());
        }
        return vo;
    }
}
