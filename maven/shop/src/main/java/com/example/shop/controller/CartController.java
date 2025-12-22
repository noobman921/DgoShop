package com.example.shop.controller;

import com.example.shop.entity.Cart;
import com.example.shop.entity.Product;
import com.example.shop.service.CartService;
import com.example.shop.service.UserService;
import com.example.shop.service.ProductService;
import com.example.shop.vo.Result;
import com.example.shop.vo.PageResultVO;
import com.example.shop.vo.CartItemDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    /**
     * 通过用户账号查询购物车记录
     *
     * @param account 用户账号（前端传递，唯一标识用户）
     * @return 统一响应结果（成功返回购物车列表，失败返回提示）
     */
    @GetMapping("/listall")
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
     * 通过用户账号分页查询购物车记录（封装到PageResultVO）
     *
     * @param account  用户账号（前端传递，唯一标识用户）
     * @param pageNo   页号（从1开始），默认1
     * @param pageSize 页尺寸（每页记录数），默认10
     * @return 统一响应结果：数据为PageResultVO<CartItemDTO>
     */
    @GetMapping("/listpage")
    public Result<PageResultVO<CartItemDTO>> getCartListByAccountWithPage(
            @RequestParam String account,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        // 1. 参数校验
        if (account == null || account.trim().isEmpty()) {
            return Result.fail("用户账号不能为空");
        }
        if (pageNo < 1) {
            return Result.fail("页号不能小于1");
        }
        if (pageSize <= 0 || pageSize > 100) {
            return Result.fail("页尺寸必须在1-100之间");
        }

        // 2. 通过账号查询用户ID
        Long userId = userService.selectUserIdByAccount(account);
        if (userId == null) {
            return Result.fail("用户账号不存在");
        }

        // 3. 核心分页逻辑
        // 3.1 查询该用户购物车总记录数（用于计算总页数）
        Integer total = cartService.countByUserId(userId);
        // 3.2 计算总页数（向上取整：(总记录数 + 页尺寸 - 1) / 页尺寸）
        Integer pages = total == 0 ? 0 : (total + pageSize - 1) / pageSize;
        // 3.3 计算偏移量（offset = (页号-1) * 页尺寸）
        Integer offset = (pageNo - 1) * pageSize;
        // 3.4 分页查询购物车列表
        List<Cart> cartList = cartService.selectByUserIdWithPage(userId, offset, pageSize);

        // 4. 转换为CartItemDTO列表（通过ProductService获取商品名称）
        List<CartItemDTO> cartItemDTOList = new ArrayList<>();
        for (Cart cart : cartList) {
            Long productId = cart.getProductId();
            Product product = productService.getProductById(productId);
            String productName = product != null ? product.getProductName() : "未知商品";

            CartItemDTO dto = new CartItemDTO();
            dto.setProductId(productId);
            dto.setProductName(productName);
            dto.setQuantity(cart.getQuantity());

            cartItemDTOList.add(dto);
        }

        // 5. 封装PageResultVO
        PageResultVO<CartItemDTO> pageResultVO = new PageResultVO<>();
        pageResultVO.setTotal(total); // 总记录数
        pageResultVO.setPages(pages); // 总页数
        pageResultVO.setPageNo(pageNo); // 当前页号
        pageResultVO.setPageSize(pageSize); // 当前页尺寸
        pageResultVO.setList(cartItemDTOList); // 分页数据列表

        // 6. 返回结果（PageResultVO作为Result的data）
        return Result.success(pageResultVO);
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