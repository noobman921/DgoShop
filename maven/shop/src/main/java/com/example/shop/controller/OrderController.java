package com.example.shop.controller;

import com.example.shop.entity.OrderItem;
import com.example.shop.entity.OrderMain;
import com.example.shop.entity.Product;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
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
    private ProductService productService; // 已注入，直接使用

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    @GetMapping("/page")
    public Result<PageResultVO<OrderDetailVO>> getOrderPage(
            @RequestParam String userAccount,
            @RequestParam(required = false) Integer pageNo,
            @RequestParam(required = false) Integer pageSize) {
        // 原有逻辑完全保留
        try {
            if (userAccount == null || userAccount.trim().isEmpty()) {
                log.error("订单分页查询失败：用户账号为空");
                return Result.fail("用户账号不能为空");
            }
            Long userId = userService.selectUserIdByAccount(userAccount);
            if (userId == null || userId <= 0) {
                log.error("订单分页查询失败：账号{}无对应用户ID", userAccount);
                return Result.fail("用户不存在");
            }
            PageResultVO<OrderMain> orderMainPage = orderMainService.listByUserIdWithPage(userId, pageNo, pageSize);
            List<OrderDetailVO> orderDetailList = new ArrayList<>();
            for (OrderMain orderMain : orderMainPage.getList()) {
                OrderDetailVO detailVO = new OrderDetailVO();
                detailVO.setOrderMain(orderMain);
                List<OrderItem> orderItemList = orderItemService.listByOrderNo(orderMain.getOrderNo());
                List<OrderItemVO> itemVOList = new ArrayList<>();
                BigDecimal totalPrice = BigDecimal.ZERO;
                for (OrderItem item : orderItemList) {
                    OrderItemVO itemVO = new OrderItemVO();
                    itemVO.setId(item.getId());
                    itemVO.setOrderNo(item.getOrderNo());
                    itemVO.setQuantity(item.getQuantity());
                    itemVO.setMerchantId(item.getMerchantId());
                    itemVO.setProductId(item.getProductId());
                    Product product = productService.getProductById(item.getProductId());
                    if (product != null) {
                        itemVO.setProductName(product.getProductName());
                        itemVO.setProductPrice(product.getProductPrice());
                    } else {
                        itemVO.setProductName("未知商品");
                        itemVO.setProductPrice(BigDecimal.ZERO);
                    }
                    itemVOList.add(itemVO);
                    if (itemVO.getProductPrice() != null) {
                        BigDecimal itemTotal = itemVO.getProductPrice()
                                .multiply(BigDecimal.valueOf(item.getQuantity()));
                        totalPrice = totalPrice.add(itemTotal);
                    }
                }
                detailVO.setItemList(itemVOList);
                detailVO.setTotalPrice(totalPrice);
                orderDetailList.add(detailVO);
            }
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

    @PostMapping("/add")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> addNewOrder(@RequestBody OrderAddRequestVO requestVO) {
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

            // ========== 新增：订单生成时扣减库存（核心改动） ==========
            for (OrderItem itemVO : requestVO.getOrderItemList()) {
                Long productId = itemVO.getProductId();
                Integer quantity = itemVO.getQuantity();
                // 调用ProductService扣减库存
                Long stockResult = productService.decreaseStock(productId, quantity);
                if (stockResult == 0L) { // 扣减失败（库存不足/商品不存在）
                    throw new RuntimeException("商品ID=" + productId + "库存扣减失败");
                }
            }

            // 原有订单生成逻辑完全保留
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

    // 简单的发货邮件接口，前端传 { toEmail, orderId }
    @PostMapping("/sendShipmentEmail")
    public Result<String> sendShipmentEmail(@RequestBody SendEmailRequest req) {
        try {
            if (req == null || req.getToEmail() == null || req.getToEmail().trim().isEmpty()) {
                log.warn("发送发货邮件失败：目标邮箱为空");
                return Result.fail("目标邮箱不能为空");
            }
            if (req.getOrderId() == null || req.getOrderId().toString().trim().isEmpty()) {
                log.warn("发送发货邮件失败：订单号为空");
                return Result.fail("订单号不能为空");
            }

            if (mailSender == null) {
                log.warn("JavaMailSender 未配置，模拟发送邮件 给 {}，orderId={} ", req.getToEmail(), req.getOrderId());
                return Result.success("邮件发送（模拟）成功");
            }

            SimpleMailMessage message = new SimpleMailMessage();
            String from = (mailFrom == null || mailFrom.trim().isEmpty()) ? "no-reply@localhost" : mailFrom;
            message.setFrom(from);
            message.setTo(req.getToEmail());
            message.setSubject("发货成功 - 订单 " + req.getOrderId());
            String text = String.format("您好，\n\n您的订单 %s 已发货。感谢您的购买！\n\n祝好，\n%s", req.getOrderId(), "商城团队");
            message.setText(text);
            mailSender.send(message);
            log.info("已发送发货邮件 到 {}，orderId={}", req.getToEmail(), req.getOrderId());
            return Result.success("邮件发送成功");
        } catch (Exception e) {
            log.error("发送发货邮件异常", e);
            return Result.fail("发送邮件失败：" + e.getMessage());
        }
    }

    // 简单请求体类
    public static class SendEmailRequest {
        private String toEmail;
        private String orderId;

        public String getToEmail() {
            return toEmail;
        }

        public void setToEmail(String toEmail) {
            this.toEmail = toEmail;
        }

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }
    }
}