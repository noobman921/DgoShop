package com.example.shop.controller;

import com.example.shop.entity.OrderItem;
import com.example.shop.entity.OrderMain;
import com.example.shop.entity.Product; // 显式导入Product实体
import com.example.shop.service.OrderItemService;
import com.example.shop.service.OrderMainService;
import com.example.shop.service.ProductService;
import com.example.shop.service.UserService;
import com.example.shop.vo.OrderAddRequestVO;
import com.example.shop.vo.OrderDetailVO;
import com.example.shop.vo.OrderItemVO;
import com.example.shop.vo.PageResultVO;
import com.example.shop.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal; // 新增BigDecimal导入
import java.util.ArrayList;
import java.util.List;

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
    @Autowired
    private ProductService productService;

    /**
     * 分页查询用户订单（适配Product实体的BigDecimal类型）
     */
    @GetMapping("/page")
    public Result<PageResultVO<OrderDetailVO>> getOrderPage(
            @RequestParam String userAccount,
            @RequestParam(required = false) Integer pageNo,
            @RequestParam(required = false) Integer pageSize) {
        try {
            // 1. 参数校验（不变）
            if (userAccount == null || userAccount.trim().isEmpty()) {
                log.error("订单分页查询失败：用户账号为空");
                return Result.fail("用户账号不能为空");
            }

            // 2. 账号转用户ID（不变）
            Long userId = userService.selectUserIdByAccount(userAccount);
            if (userId == null || userId <= 0) {
                log.error("订单分页查询失败：账号{}无对应用户ID", userAccount);
                return Result.fail("用户不存在");
            }

            // 3. 分页查询订单主表（不变）
            PageResultVO<OrderMain> orderMainPage = orderMainService.listByUserIdWithPage(userId, pageNo, pageSize);

            // 4. 构造订单详情列表（核心修改：适配BigDecimal）
            List<OrderDetailVO> orderDetailList = new ArrayList<>();
            for (OrderMain orderMain : orderMainPage.getList()) {
                OrderDetailVO detailVO = new OrderDetailVO();
                detailVO.setOrderMain(orderMain);

                // 4.1 查询订单项（不变）
                List<OrderItem> orderItemList = orderItemService.listByOrderNo(orderMain.getOrderNo());
                List<OrderItemVO> itemVOList = new ArrayList<>();

                // ========== 修改点1：总价类型从Double改为BigDecimal ==========
                BigDecimal totalPrice = BigDecimal.ZERO;

                // 4.2 封装合并后的订单项VO
                for (OrderItem item : orderItemList) {
                    OrderItemVO itemVO = new OrderItemVO();
                    // 订单项原有字段赋值（不变）
                    itemVO.setId(item.getId());
                    itemVO.setOrderNo(item.getOrderNo());
                    itemVO.setQuantity(item.getQuantity());
                    itemVO.setMerchantId(item.getMerchantId());
                    itemVO.setProductId(item.getProductId());

                    // 商品信息字段赋值（适配BigDecimal）
                    Product product = productService.getProductById(item.getProductId());
                    if (product != null) {
                        itemVO.setProductName(product.getProductName());
                        // 直接赋值BigDecimal类型的商品单价（无需类型转换）
                        itemVO.setProductPrice(product.getProductPrice());
                    } else {
                        itemVO.setProductName("未知商品");
                        // ========== 修改点2：0.00（double）改为BigDecimal.ZERO ==========
                        itemVO.setProductPrice(BigDecimal.ZERO);
                    }

                    itemVOList.add(itemVO);

                    // ========== 修改点3：总价累加改为BigDecimal精确计算 ==========
                    if (itemVO.getProductPrice() != null) {
                        // 单价 * 数量，再累加到总价（BigDecimal乘法/加法）
                        BigDecimal itemTotal = itemVO.getProductPrice()
                                .multiply(BigDecimal.valueOf(item.getQuantity()));
                        totalPrice = totalPrice.add(itemTotal);
                    }
                }

                detailVO.setItemList(itemVOList);
                // 赋值BigDecimal类型的总价（需确保OrderDetailVO的totalPrice字段也是BigDecimal）
                detailVO.setTotalPrice(totalPrice);
                orderDetailList.add(detailVO);
            }

            // 5. 构造分页VO返回（不变）
            PageResultVO<OrderDetailVO> finalPageVO = new PageResultVO<>(
                    orderMainPage.getTotal(),
                    orderMainPage.getPages(),
                    orderMainPage.getPageNo(),
                    orderMainPage.getPageSize(),
                    orderDetailList);

            log.info("用户{}订单分页查询成功：总{}条/{}页", userAccount, finalPageVO.getTotal(), finalPageVO.getPages());
            return Result.success(finalPageVO);
        } catch (Exception e) {
            log.error("用户{}订单分页查询异常", userAccount, e);
            return Result.fail("订单查询失败：" + e.getMessage());
        }
    }

    /**
     * 插入新订单（逻辑不变，保留）
     */
    @PostMapping("/add")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> addNewOrder(@RequestBody OrderAddRequestVO requestVO) {
        // 原有逻辑完全复用，无需修改
        try {
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

            Long userId = userService.selectUserIdByAccount(userAccount);
            if (userId == null || userId <= 0) {
                log.error("新增订单失败：账号{}无对应用户ID", userAccount);
                return Result.fail("用户不存在");
            }

            OrderMain orderMain = orderMainService.addOrderMain(userId);
            String orderNo = orderMain.getOrderNo();
            orderItemService.batchAddOrderItem(orderNo, requestVO.getOrderItemList());

            log.info("用户{}新增订单成功：orderNo={}，包含{}个商品",
                    userAccount, orderNo, requestVO.getOrderItemList().size());
            return Result.success("新增订单成功，订单号：" + orderNo);
        } catch (Exception e) {
            log.error("用户{}新增订单异常", requestVO != null ? requestVO.getUserAccount() : "未知", e);
            return Result.fail("新增订单失败：" + e.getMessage());
        }
    }
}