package com.dabaozi.gymmanagementsystem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dabaozi.gymmanagementsystem.common.convention.result.Result;
import com.dabaozi.gymmanagementsystem.common.convention.result.Results;
import com.dabaozi.gymmanagementsystem.pojo.dto.UserRegisterDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.UserUpdateDTO;
import com.dabaozi.gymmanagementsystem.pojo.vo.UserVO;
import com.dabaozi.gymmanagementsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理模块(register 免登录,其余需 token)
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户注册(免登录)
     * @param userRegisterDTO
     * @return
     */
    @PostMapping("api/gym-management-system/user/v1/register")
    public Result<Void> register(@RequestBody UserRegisterDTO userRegisterDTO) {
        if (userRegisterDTO.getUsername() == null || userRegisterDTO.getPassword() == null) {
            return Results.failure("401", "用户名、密码不能为空");
        }
        userService.register(userRegisterDTO);
        return Results.success();
    }

    /**
     * 修改用户信息
     * @param userUpdateDTO
     * @return
     */
    @PostMapping("api/gym-management-system/user/v1/update")
    public Result<Void> update(@RequestBody UserUpdateDTO userUpdateDTO) {
        userService.updateUser(userUpdateDTO);
        return Results.success();
    }

    /**
     * 删除用户(逻辑删除)
     * @param id
     * @return
     */
    @DeleteMapping("api/gym-management-system/user/v1/delete")
    public Result<Void> delete(@RequestParam("id") Long id) {
        userService.deleteUser(id);
        return Results.success();
    }

    /**
     * 分页查询用户(nickname/username 模糊)
     * @param current
     * @param size
     * @param nickname
     * @param username
     * @return
     */
    @GetMapping("api/gym-management-system/user/v1/page")
    public Result<Page<UserVO>> page(@RequestParam(defaultValue = "1") Long current,
                                     @RequestParam(defaultValue = "10") Long size,
                                     @RequestParam(required = false) String nickname,
                                     @RequestParam(required = false) String username) {
        return Results.success(userService.pageUser(current, size, nickname, username));
    }

    /**
     * 启用/禁用用户
     * @param id
     * @param status 1 正常 0 禁用
     * @return
     */
    @PostMapping("api/gym-management-system/user/v1/updateStatus")
    public Result<Void> updateStatus(@RequestParam("id") Long id,
                                     @RequestParam("status") Integer status) {
        userService.updateUserStatus(id, status);
        return Results.success();
    }

    /**
     * 查询用户详情
     * @param id
     * @return
     */
    @GetMapping("api/gym-management-system/user/v1/detail")
    public Result<UserVO> detail(@RequestParam("id") Long id) {
        return Results.success(userService.getUserDetail(id));
    }
}
