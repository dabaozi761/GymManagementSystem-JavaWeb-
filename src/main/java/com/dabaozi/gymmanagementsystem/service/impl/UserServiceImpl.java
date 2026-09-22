package com.dabaozi.gymmanagementsystem.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dabaozi.gymmanagementsystem.common.convention.exception.ClientException;
import com.dabaozi.gymmanagementsystem.mapper.UserMapper;
import com.dabaozi.gymmanagementsystem.pojo.dto.UserRegisterDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.UserUpdateDTO;
import com.dabaozi.gymmanagementsystem.pojo.entity.User;
import com.dabaozi.gymmanagementsystem.pojo.vo.UserVO;
import com.dabaozi.gymmanagementsystem.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public void register(UserRegisterDTO userRegisterDTO) {
        // 用户名唯一校验(数据库唯一索引兜底,这里提前给出友好提示)
        if (baseMapper.selectByUsername(userRegisterDTO.getUsername()) != null) {
            throw new ClientException("用户名已存在");
        }
        User user = User.builder()
                .username(userRegisterDTO.getUsername())
                .password(DigestUtils.md5DigestAsHex(userRegisterDTO.getPassword().getBytes()))
                .phone(userRegisterDTO.getPhone())
                .nickname(userRegisterDTO.getNickname())
                .status(1)
                .build();
        baseMapper.insert(user);
    }

    @Override
    public void updateUser(UserUpdateDTO userUpdateDTO) {
        LambdaUpdateWrapper<User> updateWrapper = Wrappers.lambdaUpdate(User.class)
                .eq(User::getId, userUpdateDTO.getId())
                .eq(User::getDelFlag, 0)
                .set(User::getNickname, userUpdateDTO.getNickname())
                .set(User::getAvatar, userUpdateDTO.getAvatar())
                .set(User::getPhone, userUpdateDTO.getPhone());
        int rows = baseMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new ClientException("用户不存在或已删除");
        }
    }

    @Override
    public void deleteUser(Long id) {
        LambdaUpdateWrapper<User> updateWrapper = Wrappers.lambdaUpdate(User.class)
                .eq(User::getId, id)
                .eq(User::getDelFlag, 0);
        int rows = baseMapper.delete(updateWrapper);
        if (rows == 0) {
            throw new ClientException("用户不存在或已删除");
        }
    }

    @Override
    public Page<UserVO> pageUser(long current, long size, String nickname, String username) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .like(StringUtils.hasText(nickname), User::getNickname, nickname)
                .like(StringUtils.hasText(username), User::getUsername, username)
                .orderByDesc(User::getCreateTime);
        Page<User> page = this.page(new Page<>(current, size), queryWrapper);
        // 实体转 VO,不带密码
        Page<UserVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(user -> BeanUtil.copyProperties(user, UserVO.class))
                .collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public void updateUserStatus(Long id, Integer status) {
        LambdaUpdateWrapper<User> updateWrapper = Wrappers.lambdaUpdate(User.class)
                .eq(User::getId, id)
                .eq(User::getDelFlag, 0)
                .set(User::getStatus, status);
        int rows = baseMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new ClientException("用户不存在或已删除");
        }
    }

    @Override
    public UserVO getUserDetail(Long id) {
        User user = baseMapper.selectById(id);
        if (user == null) {
            throw new ClientException("用户不存在或已删除");
        }
        return BeanUtil.copyProperties(user, UserVO.class);
    }
}
