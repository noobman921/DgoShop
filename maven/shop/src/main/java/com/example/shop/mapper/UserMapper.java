package com.example.shop.mapper;

import com.example.shop.entity.User;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface UserMapper {
    /**
     * 根据用户ID查询（主键查询）
     * 
     * @param userId 用户主键ID
     * @return 用户实体
     */
    @Select("SELECT * FROM user WHERE user_id = #{userId}")
    User selectByUserId(Long userId);

    /**
     * 根据用户账号查询
     * 
     * @param account 用户账号
     * @return 用户实体
     */
    @Select("SELECT * FROM user WHERE account = #{account}")
    User selectByAccount(String account);

    /**
     * 根据用户名查询
     * 
     * @param username 用户名
     * @return 用户实体
     */
    @Select("SELECT * FROM user WHERE username = #{username}")
    List<User> selectListByUsername(String username);

    /**
     * 插入用户信息
     * 
     * @param user 用户实体
     * @return 插入结果，影响的行数
     */
    @Insert("INSERT INTO user (username, account, password) " +
            "VALUES (#{username}, #{account}, #{password}")
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    int insertUser(User user);
}
