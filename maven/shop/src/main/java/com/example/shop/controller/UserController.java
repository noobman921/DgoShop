package com.example.shop.controller;

import com.example.shop.vo.Result;
import com.example.shop.entity.User;
import com.example.shop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user/log")
public class UserController {

    /**
     * 注入用户业务层
     */
    @Autowired
    private UserService userService;

    /**
     * 用户登录接口
     * 
     * @param username 用户名
     * @param password 密码
     * @return 统一响应结果
     */
    @GetMapping("/login")
    public Result<User> login(
            @RequestParam("username") String username,
            @RequestParam("password") String password) {
        // 根据用户名查询用户列表
        List<User> userList = userService.selectListByUsername(username);
        if (userList == null || userList.isEmpty()) {
            return Result.fail("用户名不存在");
        }

        // 验证密码
        User user = userList.get(0);
        if (!password.equals(user.getPassword())) {
            return Result.fail("密码错误");
        }

        // 登录成功
        return Result.success(user);
    }

    /**
     * 用户注册接口
     * 
     * @param username 用户名
     * @param password 密码
     * @return 统一响应结果
     */
    @GetMapping("/reg")
    public Result<String> register(
            @RequestParam("username") String username,
            @RequestParam("password") String password) {
        // 校验用户名是否已存在
        List<User> userList = userService.selectListByUsername(username);
        if (userList != null && !userList.isEmpty()) {
            return Result.fail("用户名已存在");
        }

        // 构造用户对象并插入
        User user = new User();
        user.setUsername(username);
        user.setAccount(username);
        user.setPassword(password);
        int insertCount = userService.insertUser(user);

        // 判断插入结果
        if (insertCount > 0) {
            return Result.success("注册成功");
        } else {
            return Result.fail("注册失败，请重试");
        }
    }
}