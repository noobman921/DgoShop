package com.example.shop.service;

import com.example.shop.entity.Cart;
import com.example.shop.mapper.CartMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

@Service
public class CartService {

    /**
     * 注入购物车数据访问层接口
     */
    @Autowired
    private CartMapper cartMapper;

    /**
     * 根据用户ID查询购物车列表
     * 
     * @param userId 用户ID
     * @return 购物车实体列表
     */
    public List<Cart> selectByUserId(Long userId) {
        // 参数校验：用户ID不能为空
        Assert.notNull(userId, "用户ID不能为空");
        return cartMapper.selectByUserId(userId);
    }

    /**
     * 根据商品ID查询购物车列表
     * 
     * @param productId 商品ID
     * @return 购物车实体列表
     */
    public List<Cart> selectByProductId(Long productId) {
        // 参数校验：商品ID不能为空
        Assert.notNull(productId, "商品ID不能为空");
        return cartMapper.selectByProductId(productId);
    }

    /**
     * 插入购物车信息
     * 
     * @param cart 购物车实体
     * @return 插入结果，影响的行数
     */
    public int insertCart(Cart cart) {
        // 参数校验：购物车实体、用户ID、商品ID、数量不能为空且数量大于0
        Assert.notNull(cart, "购物车实体不能为空");
        Assert.notNull(cart.getUserId(), "用户ID不能为空");
        Assert.notNull(cart.getProductId(), "商品ID不能为空");
        Assert.notNull(cart.getQuantity(), "商品数量不能为空");
        Assert.isTrue(cart.getQuantity() > 0, "商品数量必须大于0");
        return cartMapper.insertCart(cart);
    }

    /**
     * 更新购物车商品数量（累加）
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @param quantity  新数量（累加）
     * @return 影响行数
     */
    public int updateQuantityByUserIdAndProductId(Long userId, Long productId, Integer quantity) {
        // 参数校验：用户ID、商品ID不能为空，累加数量不能小于等于0
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(productId, "商品ID不能为空");
        Assert.notNull(quantity, "累加数量不能为空");
        Assert.isTrue(quantity != 0, "累加数量不能为0"); // 支持增加（正数）或减少（负数），但不能为0
        return cartMapper.updateQuantityByUserIdAndProductId(userId, productId, quantity);
    }

    /**
     * 按用户ID+商品ID删除单条购物车记录（精准删除）
     *
     * @param userId    用户ID
     * @param productId 商品ID
     * @return 影响的行数（1=删除成功，0=无匹配记录）
     */
    public int deleteByUserIdAndProductId(Long userId, Long productId) {
        // 参数校验：用户ID、商品ID不能为空
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(productId, "商品ID不能为空");
        return cartMapper.deleteByUserIdAndProductId(userId, productId);
    }
}