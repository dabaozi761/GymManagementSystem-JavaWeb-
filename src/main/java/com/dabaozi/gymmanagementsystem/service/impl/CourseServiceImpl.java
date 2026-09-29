package com.dabaozi.gymmanagementsystem.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dabaozi.gymmanagementsystem.common.convention.exception.ClientException;
import com.dabaozi.gymmanagementsystem.common.convention.utils.BusinessNoGenerator;
import com.dabaozi.gymmanagementsystem.mapper.CourseEnrollmentMapper;
import com.dabaozi.gymmanagementsystem.mapper.CourseMapper;
import com.dabaozi.gymmanagementsystem.pojo.dto.CoursePageReqDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.CourseSaveDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.CourseUpdateDTO;
import com.dabaozi.gymmanagementsystem.pojo.entity.Course;
import com.dabaozi.gymmanagementsystem.pojo.entity.CourseEnrollment;
import com.dabaozi.gymmanagementsystem.pojo.vo.CoursePageRespVO;
import com.dabaozi.gymmanagementsystem.pojo.vo.CourseVO;
import com.dabaozi.gymmanagementsystem.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 课程服务实现
 */
@Service
@RequiredArgsConstructor
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    /**
     * 课程类型范围:1 团课 2 私教 3 其他
     */
    private static final int MIN_TYPE = 1;
    private static final int MAX_TYPE = 3;

    /**
     * 开课状态
     */
    private static final int OPEN_STATUS = 1;

    /**
     * 报名状态:已报名
     */
    private static final int ENROLLED_STATUS = 1;

    private final CourseEnrollmentMapper courseEnrollmentMapper;

    @Override
    public void saveCourse(CourseSaveDTO dto) {
        if (!StringUtils.hasText(dto.getName())) {
            throw new ClientException("课程名称不能为空");
        }
        if (dto.getType() == null) {
            throw new ClientException("课程类型不能为空(1团课 2私教 3其他)");
        }
        validateCommon(dto.getType(), dto.getPrice(), dto.getCapacity());
        Integer status = dto.getStatus() == null ? OPEN_STATUS : dto.getStatus();
        validateStatus(status);
        Course course = Course.builder()
                .courseNo(BusinessNoGenerator.nextCourseNo())
                .name(dto.getName())
                .type(dto.getType())
                .coachName(dto.getCoachName())
                .price(dto.getPrice())
                .capacity(dto.getCapacity())
                .status(status)
                .remark(dto.getRemark())
                .build();
        baseMapper.insert(course);
    }

    @Override
    public void updateCourse(CourseUpdateDTO dto) {
        if (dto.getId() == null) {
            throw new ClientException("课程id不能为空");
        }
        requireCourse(dto.getId());
        validateCommon(dto.getType(), dto.getPrice(), dto.getCapacity());
        if (dto.getStatus() != null) {
            validateStatus(dto.getStatus());
        }
        // 课程编号不可修改,这里不复制 courseNo
        Course course = Course.builder()
                .name(dto.getName())
                .type(dto.getType())
                .coachName(dto.getCoachName())
                .price(dto.getPrice())
                .capacity(dto.getCapacity())
                .status(dto.getStatus())
                .remark(dto.getRemark())
                .build();
        baseMapper.update(course, Wrappers.lambdaUpdate(Course.class)
                .eq(Course::getId, dto.getId()));
    }

    @Override
    public void deleteCourse(Long id) {
        requireCourse(id);
        // 有有效报名记录(已报名)的课程不允许删除
        Long enrolledCount = courseEnrollmentMapper.selectCount(Wrappers.lambdaQuery(CourseEnrollment.class)
                .eq(CourseEnrollment::getCourseId, id)
                .eq(CourseEnrollment::getStatus, ENROLLED_STATUS));
        if (enrolledCount != null && enrolledCount > 0) {
            throw new ClientException("该课程存在已报名记录，不能删除");
        }
        baseMapper.deleteById(id);
    }

    @Override
    public void updateCourseStatus(Long id, Integer status) {
        validateStatus(status);
        requireCourse(id);
        baseMapper.update(null, Wrappers.lambdaUpdate(Course.class)
                .eq(Course::getId, id)
                .set(Course::getStatus, status));
    }

    @Override
    public CourseVO getCourse(Long id) {
        return BeanUtil.toBean(requireCourse(id), CourseVO.class);
    }

    @Override
    public CoursePageRespVO pageCourse(CoursePageReqDTO dto) {
        if (dto == null) {
            dto = new CoursePageReqDTO();
        }
        LambdaQueryWrapper<Course> wrapper = Wrappers.lambdaQuery(Course.class)
                .like(StringUtils.hasText(dto.getName()), Course::getName, dto.getName())
                .like(StringUtils.hasText(dto.getCoachName()), Course::getCoachName, dto.getCoachName())
                .eq(dto.getType() != null, Course::getType, dto.getType())
                .eq(dto.getStatus() != null, Course::getStatus, dto.getStatus())
                .orderByDesc(Course::getCreateTime);
        Page<Course> page = new Page<>(dto.getCurrent(), dto.getSize());
        IPage<Course> pageResult = baseMapper.selectPage(page, wrapper);
        List<CourseVO> voList = pageResult.getRecords().stream()
                .map(course -> BeanUtil.toBean(course, CourseVO.class))
                .toList();
        return CoursePageRespVO.builder()
                .total(pageResult.getTotal())
                .records(voList)
                .current(pageResult.getCurrent())
                .size(pageResult.getSize())
                .pages(pageResult.getPages())
                .build();
    }

    /**
     * 校验课程类型/价格/人数上限(为 null 的字段跳过校验)
     */
    private void validateCommon(Integer type, BigDecimal price, Integer capacity) {
        if (type != null && (type < MIN_TYPE || type > MAX_TYPE)) {
            throw new ClientException("课程类型只能是1至3(1团课 2私教 3其他)");
        }
        if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {
            throw new ClientException("课程价格不能小于0");
        }
        if (capacity != null && capacity < 0) {
            throw new ClientException("报名人数上限不能小于0");
        }
    }

    /**
     * 校验课程状态 0/1
     */
    private void validateStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new ClientException("课程状态只能是0(停课)或1(开课)");
        }
    }

    private Course requireCourse(Long id) {
        if (id == null) {
            throw new ClientException("课程id不能为空");
        }
        Course course = baseMapper.selectById(id);
        if (course == null) {
            throw new ClientException("课程不存在或已删除");
        }
        return course;
    }
}
