package com.dabaozi.gymmanagementsystem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dabaozi.gymmanagementsystem.pojo.dto.UserRegisterDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.UserUpdateDTO;
import com.dabaozi.gymmanagementsystem.pojo.entity.User;
import com.dabaozi.gymmanagementsystem.pojo.vo.UserVO;

public interface UserService extends IService<User> {

    /**
     * 用户注册(免登录)
     * @param userRegisterDTO
     */
    void register(UserRegisterDTO userRegisterDTO);

    /**
     * 修改用户信息
     * @param userUpdateDTO
     */
    void updateUser(UserUpdateDTO userUpdateDTO);

    /**
     * 删除用户(逻辑删除)
     * @param id
     */
    void deleteUser(Long id);

    /**
     * 分页查询用户(nickname/username 模糊)
     * @param current
     * @param size
     * @param nickname
     * @param username
     * @return
     */
    Page<UserVO> pageUser(long current, long size, String nickname, String username);

    /**
     * 启用/禁用用户
     * @param id
     * @param status 1 正常 0 禁用
     */
    void updateUserStatus(Long id, Integer status);

    /**
     * 查询用户详情
     * @param id
     * @return
     */
    UserVO getUserDetail(Long id);
}
