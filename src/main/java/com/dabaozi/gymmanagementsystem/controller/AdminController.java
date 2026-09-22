package com.dabaozi.gymmanagementsystem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dabaozi.gymmanagementsystem.common.convention.result.Result;
import com.dabaozi.gymmanagementsystem.common.convention.result.Results;
import com.dabaozi.gymmanagementsystem.pojo.dto.AdminSaveDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.AdminUpdateDTO;
import com.dabaozi.gymmanagementsystem.pojo.vo.AdminVO;
import com.dabaozi.gymmanagementsystem.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员管理模块(仅管理员 role=1 可访问,拦截器校验)
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * 新增管理员
     * @param adminSaveDTO
     * @return
     */
    @PostMapping("api/gym-management-system/admin/v1/save")
    public Result<Void> save(@RequestBody AdminSaveDTO adminSaveDTO) {
        if (adminSaveDTO.getUsername() == null || adminSaveDTO.getPassword() == null) {
            return Results.failure("401", "用户名、密码不能为空");
        }
        adminService.saveAdmin(adminSaveDTO);
        return Results.success();
    }

    /**
     * 删除管理员(逻辑删除)
     * @param id
     * @return
     */
    @DeleteMapping("api/gym-management-system/admin/v1/delete")
    public Result<Void> delete(@RequestParam("id") Long id) {
        adminService.deleteAdmin(id);
        return Results.success();
    }

    /**
     * 修改管理员(password 为空则不修改密码)
     * @param adminUpdateDTO
     * @return
     */
    @PostMapping("api/gym-management-system/admin/v1/update")
    public Result<Void> update(@RequestBody AdminUpdateDTO adminUpdateDTO) {
        adminService.updateAdmin(adminUpdateDTO);
        return Results.success();
    }

    /**
     * 分页查询管理员(name/username 模糊)
     * @param current
     * @param size
     * @param name
     * @param username
     * @return
     */
    @GetMapping("api/gym-management-system/admin/v1/page")
    public Result<Page<AdminVO>> page(@RequestParam(defaultValue = "1") Long current,
                                      @RequestParam(defaultValue = "10") Long size,
                                      @RequestParam(required = false) String name,
                                      @RequestParam(required = false) String username) {
        return Results.success(adminService.pageAdmin(current, size, name, username));
    }

    /**
     * 启用/禁用管理员
     * @param id
     * @param status 1 正常 0 禁用
     * @return
     */
    @PostMapping("api/gym-management-system/admin/v1/updateStatus")
    public Result<Void> updateStatus(@RequestParam("id") Long id,
                                     @RequestParam("status") Integer status) {
        adminService.updateAdminStatus(id, status);
        return Results.success();
    }
}
