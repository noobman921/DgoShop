package com.example.shop.controller;

import com.example.shop.entity.OrderMain;
import com.example.shop.entity.OrderItem;
import com.example.shop.service.OrderItemService;
import com.example.shop.service.OrderMainService;
import com.example.shop.service.UserService;
import com.example.shop.vo.OrderAddRequestVO;
import com.example.shop.vo.OrderDetailVO;
import com.example.shop.vo.PageResultVO;
import com.example.shop.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.util.CollectionUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单Controller（返回格式与购物车完全对齐，复用前端分页/渲染逻辑）
 */
@Slf4j
@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private UserService userService;
    @Autowired
    private OrderMainService orderMainService;
    @Autowired
    private OrderItemService orderItemService;

    /**
     * 分页查询用户订单（格式与购物车一致，前端可复用购物车的展示逻辑）
     * 
     * @param userAccount 用户账号（必传，和购物车的用户账号参数一致）
     * @param pageNo      页号（默认1，和购物车分页参数一致）
     * @param pageSize    页尺寸（默认4，和购物车分页参数一致）
     * @return ResultVO<PageResultVO<OrderDetailVO>> - 与购物车返回格式100%对齐
     */
    @GetMapping("/page") // 接口路径与购物车保持一致（如购物车是/api/cart/page，订单是/api/order/page）
    public Result<PageResultVO<OrderDetailVO>> getOrderPage(
            @RequestParam String userAccount,
            @RequestParam(required = false) Integer pageNo,
            @RequestParam(required = false) Integer pageSize) {
        try {
            // 1. 基础参数校验（和购物车的参数校验逻辑一致）
            if (userAccount == null || userAccount.trim().isEmpty()) {
                log.error("订单分页查询失败：用户账号为空");
                return Result.fail("用户账号不能为空");
            }

            // 2. 账号→用户ID（和购物车的用户ID查询逻辑一致）
            Long userId = userService.selectUserIdByAccount(userAccount);
            if (userId == null || userId <= 0) {
                log.error("订单分页查询失败：账号{}无对应用户ID", userAccount);
                return Result.fail("用户不存在");
            }

            // 3. 分页查询订单主表（复用已有的分页逻辑，默认页尺寸=4）
            PageResultVO<OrderMain> orderMainPage = orderMainService.listByUserIdWithPage(userId, pageNo, pageSize);

            // 4. 构造订单详情列表（对应购物车的项列表）
            List<OrderDetailVO> orderDetailList = new ArrayList<>();
            for (OrderMain orderMain : orderMainPage.getList()) {
                // 4.1 按订单号查询订单项目（对应购物车项的商品信息）
                List<OrderItem> orderItemList = orderItemService.listByOrderNo(orderMain.getOrderNo());

                // 4.2 封装单条订单详情（对应购物车的单条项）
                OrderDetailVO orderDetailVO = new OrderDetailVO();
                orderDetailVO.setOrderMain(orderMain);
                orderDetailVO.setOrderItemList(orderItemList);

                orderDetailList.add(orderDetailVO);
            }

            // 5. 构造与购物车格式一致的PageResultVO（核心：对齐字段顺序）
            // PageResultVO全参构造顺序：total, pages, pageNo, pageSize, list
            PageResultVO<OrderDetailVO> finalPageVO = new PageResultVO<>(
                    orderMainPage.getTotal(), // 总记录数（订单主数量，对应购物车项总数）
                    orderMainPage.getPages(), // 总页数（对应购物车总页数）
                    orderMainPage.getPageNo(), // 当前页号（对应购物车当前页）
                    orderMainPage.getPageSize(), // 页尺寸（默认4，对应购物车页尺寸）
                    orderDetailList // 订单详情列表（对应购物车项列表）
            );

            log.info("用户{}订单分页查询成功：总{}条/{}页，当前页{}条订单",
                    userAccount, finalPageVO.getTotal(), finalPageVO.getPages(), finalPageVO.getList().size());

            // 6. 返回与购物车完全一致的格式
            return Result.success(finalPageVO);
        } catch (Exception e) {
            log.error("用户{}订单分页查询异常", userAccount, e);
            return Result.fail("订单查询失败：" + e.getMessage());
        }
    }

    /**
     * 插入新订单（商家ID仅在OrderItem中）
     */
    @PostMapping("/add")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> addNewOrder(@RequestBody OrderAddRequestVO requestVO) {
        try {
            // 1. 参数校验
            if (requestVO == null) {
                return Result.fail("请求参数不能为空");
            }
            String userAccount = requestVO.getUserAccount();
            if (userAccount == null || userAccount.trim().isEmpty()) {
                log.error("新增订单失败：用户账号为空");
                return Result.fail("用户账号不能为空");
            }
            if (CollectionUtils.isEmpty(requestVO.getOrderItemList())) {
                log.error("新增订单失败：用户{}无订单项目", userAccount);
                return Result.fail("订单商品列表不能为空");
            }

            // 2. 账号→用户ID
            Long userId = userService.selectUserIdByAccount(userAccount);
            if (userId == null || userId <= 0) {
                log.error("新增订单失败：账号{}无对应用户ID", userAccount);
                return Result.fail("用户不存在");
            }

            // 3. 插入订单主表（无需商家ID）
            OrderMain orderMain = orderMainService.addOrderMain(userId); // 移除merchantId参数
            String orderNo = orderMain.getOrderNo();

            // 4. 批量插入订单项目（校验每个项的merchantId）
            orderItemService.batchAddOrderItem(orderNo, requestVO.getOrderItemList());

            log.info("用户{}新增订单成功：orderNo={}，包含{}个商品（分属不同商家）",
                    userAccount, orderNo, requestVO.getOrderItemList().size());

            return Result.success("新增订单成功，订单号：" + orderNo);
        } catch (Exception e) {
            log.error("用户{}新增订单异常", requestVO != null ? requestVO.getUserAccount() : "未知", e);
            return Result.fail("新增订单失败：" + e.getMessage());
        }
    }
}