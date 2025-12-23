package com.example.shop.mapper;

import com.example.shop.entity.Cart;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface CartMapper {
        /**
         * 根据用户ID查询
         * 
         * @param userId 用户ID
         * @return 购物车实体列表
         */
        @Select("SELECT * FROM cart WHERE user_id = #{userId}")
        List<Cart> selectByUserId(Long userId);

        /**
         * 根据用户ID页查询
         * 
         * @param userId 用户ID
         * @param offset 偏移量
         * @param limit  页限制
         * @return 购物车实体列表
         */
        @Select("SELECT product_id, user_id, quantity FROM cart WHERE user_id = #{userId} LIMIT #{offset}, #{limit}")
        List<Cart> selectByUserIdWithPage(
                        @Param("userId") Long userId,
                        @Param("offset") Integer offset,
                        @Param("limit") Integer limit);

        /**
         * 根据用户ID查询购物车记录数
         * 
         * @param userId 用户ID
         * @return 购物车记录数
         */
        @Select("SELECT COUNT(*) FROM cart WHERE user_id = #{userId}")
        Integer countByUserId(@Param("userId") Long userId);

        /**
         * 根据商品ID查询
         * 
         * @param productId 商品ID
         * @return 购物车实体
         */
        @Select("SELECT * FROM cart WHERE product_id = #{productId}")
        List<Cart> selectByProductId(Long productId);

        /**
         * 插入购物车信息
         * 
         * @param cart 购物车实体
         * @return 插入结果，影响的行数
         */
        @Insert("INSERT INTO cart (user_id, product_id, quantity) VALUES (#{userId}, #{productId}, #{quantity}) " +
                        "ON DUPLICATE KEY UPDATE quantity = quantity + #{quantity}")
        int insertCart(Cart cart);

        /**
         * 更新购物车商品数量
         *
         * @param userId    用户ID
         * @param productId 商品ID
         * @param quantity  新数量（累加）
         * @return 影响行数
         */
        @Update("UPDATE cart SET quantity = quantity + #{quantity} WHERE user_id = #{userId} AND product_id = #{productId}")
        int updateQuantityByUserIdAndProductId(
                        @Param("userId") Long userId,
                        @Param("productId") Long productId,
                        @Param("quantity") Integer quantity);

        /**
         * 按用户ID+商品ID删除单条购物车记录（精准删除）
         *
         * @param userId    用户ID
         * @param productId 商品ID
         * @return 影响的行数（1=删除成功，0=无匹配记录）
         */
        @Delete("DELETE FROM cart WHERE user_id = #{userId} AND product_id = #{productId}")
        int deleteByUserIdAndProductId(
                        @Param("userId") Long userId,
                        @Param("productId") Long productId);
}