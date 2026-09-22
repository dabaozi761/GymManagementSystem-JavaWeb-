package com.dabaozi.gymmanagementsystem.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dabaozi.gymmanagementsystem.common.convention.exception.ClientException;
import com.dabaozi.gymmanagementsystem.mapper.AdminMapper;
import com.dabaozi.gymmanagementsystem.pojo.dto.AdminSaveDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.AdminUpdateDTO;
import com.dabaozi.gymmanagementsystem.pojo.entity.Admin;
import com.dabaozi.gymmanagementsystem.pojo.vo.AdminVO;
import com.dabaozi.gymmanagementsystem.service.AdminService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Override
    public void saveAdmin(AdminSaveDTO adminSaveDTO) {
        // 用户名唯一校验(数据库唯一索引兜底,这里提前给出友好提示)
        if (baseMapper.selectByUsername(adminSaveDTO.getUsername()) != null) {
            throw new ClientException("用户名已存在");
        }
        Admin admin = Admin.builder()
                .username(adminSaveDTO.getUsername())
                .password(DigestUtils.md5DigestAsHex(adminSaveDTO.getPassword().getBytes()))
                .name(adminSaveDTO.getName())
                .phone(adminSaveDTO.getPhone())
                .status(1)
                .build();
        baseMapper.insert(admin);
    }

    @Override
    public void deleteAdmin(Long id) {
        LambdaUpdateWrapper<Admin> updateWrapper = Wrappers.lambdaUpdate(Admin.class)
                .eq(Admin::getId, id)
                .eq(Admin::getDelFlag, 0);
        int rows = baseMapper.delete(updateWrapper);
        if (rows == 0) {
            throw new ClientException("管理员不存在或已删除");
        }
    }

    @Override
    public void updateAdmin(AdminUpdateDTO adminUpdateDTO) {
        LambdaUpdateWrapper<Admin> updateWrapper = Wrappers.lambdaUpdate(Admin.class)
                .eq(Admin::getId, adminUpdateDTO.getId())
                .eq(Admin::getDelFlag, 0)
                .set(Admin::getName, adminUpdateDTO.getName())
                .set(Admin::getPhone, adminUpdateDTO.getPhone());
        // 密码非空才修改
        if (StringUtils.hasText(adminUpdateDTO.getPassword())) {
            updateWrapper.set(Admin::getPassword, DigestUtils.md5DigestAsHex(adminUpdateDTO.getPassword().getBytes()));
        }
        int rows = baseMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new ClientException("管理员不存在或已删除");
        }
    }

    @Override
    public Page<AdminVO> pageAdmin(long current, long size, String name, String username) {
        LambdaQueryWrapper<Admin> queryWrapper = Wrappers.lambdaQuery(Admin.class)
                .like(StringUtils.hasText(name), Admin::getName, name)
                .like(StringUtils.hasText(username), Admin::getUsername, username)
                .orderByDesc(Admin::getCreateTime);
        Page<Admin> page = this.page(new Page<>(current, size), queryWrapper);
        // 实体转 VO,不带密码
        Page<AdminVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(admin -> BeanUtil.copyProperties(admin, AdminVO.class))
                .collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public void updateAdminStatus(Long id, Integer status) {
        LambdaUpdateWrapper<Admin> updateWrapper = Wrappers.lambdaUpdate(Admin.class)
                .eq(Admin::getId, id)
                .eq(Admin::getDelFlag, 0)
                .set(Admin::getStatus, status);
        int rows = baseMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new ClientException("管理员不存在或已删除");
        }
    }
}
