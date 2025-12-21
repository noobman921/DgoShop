package com.example.shop.controller;

import com.example.shop.entity.Cart;
import com.example.shop.service.CartService;
import com.example.shop.service.UserService;
import com.example.shop.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    /**
     * 通过用户账号查询购物车记录
     *
     * @param account 用户账号（前端传递，唯一标识用户）
     * @return 统一响应结果（成功返回购物车列表，失败返回提示）
     */
    @GetMapping("/list")
    public Result<List<Cart>> getCartListByAccount(@RequestParam String account) {
        // 参数校验：账号不能为空
        if (account == null || account.trim().isEmpty()) {
            return Result.fail("用户账号不能为空");
        }

        // 通过账号查询用户ID
        Long userId = userService.selectUserIdByAccount(account);
        if (userId == null) {
            return Result.fail("用户账号不存在");
        }

        // 查询该用户的购物车列表
        List<Cart> cartList = cartService.selectByUserId(userId);

        // 返回成功响应
        return Result.success(cartList);
    }

    /**
     * 新增购物车记录
     *
     * @param account   用户账号（前端传递）
     * @param productId 商品ID（前端传递）
     * @param quantity  商品数量（前端传递，需大于0）
     * @return 统一响应结果
     */
    @PostMapping("/add")
    public Result<String> addCart(
            @RequestParam("account") String account,
            @RequestParam("productId") Long productId,
            @RequestParam("quantity") Integer quantity) {
        // 基础参数校验
        if (account == null || account.trim().isEmpty()) {
            return Result.fail("用户账号不能为空");
        }
        if (productId == null || productId <= 0) {
            return Result.fail("商品ID不合法（需为正整数）");
        }
        if (quantity == null || quantity <= 0) {
            return Result.fail("商品数量不合法（需为正整数）");
        }

        // 通过账号查询用户ID
        Long userId = userService.selectUserIdByAccount(account);
        if (userId == null) {
            return Result.fail("用户账号不存在");
        }

        // 构建购物车对象
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setProductId(productId);
        cart.setQuantity(quantity);

        // 调用业务层新增购物车
        int insertResult = cartService.insertCart(cart);
        if (insertResult > 0) {
            return Result.success("新增购物车成功");
        } else {
            return Result.fail("新增购物车失败，请重试");
        }
    }

    /**
     * 删除购物车记录（按用户账号+商品ID精准删除）
     *
     * @param account   用户账号（前端传递）
     * @param productId 商品ID（前端传递）
     * @return 统一响应结果
     */
    @PostMapping("/delete")
    public Result<String> deleteCart(
            @RequestParam("account") String account,
            @RequestParam("productId") Long productId) {
        // 基础参数校验
        if (account == null || account.trim().isEmpty()) {
            return Result.fail("用户账号不能为空");
        }
        if (productId == null || productId <= 0) {
            return Result.fail("商品ID不合法（需为正整数）");
        }

        // 通过账号查询用户ID
        Long userId = userService.selectUserIdByAccount(account);
        if (userId == null) {
            return Result.fail("用户账号不存在");
        }

        // 调用业务层删除购物车记录
        int deleteResult = cartService.deleteByUserIdAndProductId(userId, productId);
        if (deleteResult > 0) {
            return Result.success("删除购物车记录成功");
        } else {
            return Result.fail("删除失败（未找到匹配的购物车记录）");
        }
    }
}