package com.example.shop.service;

import com.example.shop.entity.User;
import com.example.shop.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    /**
     * 注入用户数据访问层接口
     */
    @Autowired
    private UserMapper userMapper;

    /**
     * 根据用户ID查询（主键查询）
     * 
     * @param userId 用户主键ID
     * @return 用户实体
     */
    public User selectByUserId(Long userId) {
        return userMapper.selectByUserId(userId);
    }

    /**
     * 根据用户账号查询
     * 
     * @param account 用户账号
     * @return 用户实体
     */
    public User selectByAccount(String account) {
        return userMapper.selectByAccount(account);
    }

    /**
     * 根据用户名查询
     * 
     * @param username 用户名
     * @return 用户实体列表
     */
    public List<User> selectListByUsername(String username) {
        return userMapper.selectListByUsername(username);
    }

    /**
     * 插入用户信息
     * 
     * @param user 用户实体
     * @return 插入结果，影响的行数
     */
    public int insertUser(User user) {
        return userMapper.insertUser(user);
    }
}
