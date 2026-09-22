package com.dabaozi.gymmanagementsystem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dabaozi.gymmanagementsystem.pojo.dto.AdminSaveDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.AdminUpdateDTO;
import com.dabaozi.gymmanagementsystem.pojo.entity.Admin;
import com.dabaozi.gymmanagementsystem.pojo.vo.AdminVO;

public interface AdminService extends IService<Admin> {

    /**
     * 新增管理员
     * @param adminSaveDTO
     */
    void saveAdmin(AdminSaveDTO adminSaveDTO);

    /**
     * 删除管理员(逻辑删除)
     * @param id
     */
    void deleteAdmin(Long id);

    /**
     * 修改管理员(password 为空则不修改密码)
     * @param adminUpdateDTO
     */
    void updateAdmin(AdminUpdateDTO adminUpdateDTO);

    /**
     * 分页查询管理员(name/username 模糊)
     * @param current
     * @param size
     * @param name
     * @param username
     * @return
     */
    Page<AdminVO> pageAdmin(long current, long size, String name, String username);

    /**
     * 启用/禁用管理员
     * @param id
     * @param status 1 正常 0 禁用
     */
    void updateAdminStatus(Long id, Integer status);
}
