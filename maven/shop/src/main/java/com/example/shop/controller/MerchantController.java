package com.example.shop.controller;

import com.example.shop.entity.Merchant;
import com.example.shop.entity.OrderItem;
import com.example.shop.entity.Product;
import com.example.shop.service.MerchantService;
import com.example.shop.service.OrderItemService;
import com.example.shop.service.ProductService;
import com.example.shop.vo.PageResultVO;
import com.example.shop.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 商家专属Controller
 * 风格对齐UserController：GET请求+RequestParam传参+Result响应体
 * 提供：1.商家注册 2.商家登录 3.按商家ID分页查订单（含商品名称）；4.按商家ID分页查商品
 */
@Slf4j
@RestController
@RequestMapping("/api/merchant/log") // 对齐用户接口路径风格 /api/xxx/log
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private ProductService productService;

    /**
     * 商家登录接口
     * 前端请求：GET /api/merchant/log/login?account=xxx&password=xxx
     * 入参：account（商家账号）、password（密码）
     * 返回：统一响应结果Result<Merchant>
     */
    @GetMapping("/login")
    public Result<Merchant> login(
            @RequestParam("account") String account,
            @RequestParam("password") String password) {
        // 参数非空校验
        if (!StringUtils.hasText(account)) {
            log.error("商家登录失败：账号为空");
            return Result.fail("账号不能为空");
        }
        if (!StringUtils.hasText(password)) {
            log.error("商家登录失败：密码为空");
            return Result.fail("密码不能为空");
        }

        // 根据账号查询商家
        Merchant merchant = merchantService.getMerchantByAccount(account);
        if (merchant == null) {
            log.warn("商家登录失败：账号不存在，account={}", account);
            return Result.fail("账号不存在");
        }

        // 验证密码（生产环境建议加密对比）
        if (!password.equals(merchant.getPassword())) {
            log.warn("商家登录失败：密码错误，account={}", account);
            return Result.fail("密码错误");
        }

        // 登录成功
        log.info("商家登录成功，account={}", account);
        return Result.success(merchant);
    }

    /**
     * 商家注册接口
     * 前端请求：GET /api/merchant/log/reg?merchantName=xxx&account=xxx&password=xxx
     * 入参：merchantName（商家名称）、account（账号）、password（密码）
     * 返回：统一响应结果Result<String>
     */
    @GetMapping("/reg")
    public Result<String> register(
            @RequestParam("merchantName") String merchantName,
            @RequestParam("account") String account,
            @RequestParam("password") String password) {
        // 参数非空校验
        if (!StringUtils.hasText(merchantName)) {
            log.error("商家注册失败：商家名称为空");
            return Result.fail("商家名称不能为空");
        }
        if (!StringUtils.hasText(account)) {
            log.error("商家注册失败：账号为空");
            return Result.fail("账号不能为空");
        }
        if (!StringUtils.hasText(password)) {
            log.error("商家注册失败：密码为空");
            return Result.fail("密码不能为空");
        }

        // 校验账号是否已存在
        Merchant existMerchant = merchantService.getMerchantByAccount(account);
        if (existMerchant != null) {
            log.warn("商家注册失败：账号已存在，account={}", account);
            return Result.fail("账号已存在");
        }

        // 构造商家对象并插入
        Merchant merchant = new Merchant();
        merchant.setMerchantName(merchantName);
        merchant.setAccount(account);
        merchant.setPassword(password); // 生产环境需加密密码

        // 调用Service注册
        boolean registerSuccess = merchantService.addMerchant(merchant);
        if (registerSuccess) {
            log.info("商家注册成功，account={}", account);
            return Result.success("注册成功");
        } else {
            log.error("商家注册失败：数据库插入失败，account={}", account);
            return Result.fail("注册失败，请重试");
        }
    }

    /**
     * 接口：按商家ID分页查询订单项（含商品名称）
     * 前端请求：GET
     * /api/merchant/log/order-item/page?merchantId=xxx&pageNo=1&pageSize=10
     * 返回：分页结果（每页10条），包含「商品名称 + 完整OrderItem」
     */
    @GetMapping("/order-item/page")
    public PageResultVO<OrderItemWithProductName> getOrderItemByMerchantIdPage(
            @RequestParam Long merchantId,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            // 参数校验
            if (merchantId == null || merchantId <= 0) {
                log.error("按商家ID查订单失败：商家ID不合法，merchantId={}", merchantId);
                return new PageResultVO<>(0, 0, pageNo, pageSize, Collections.emptyList());
            }

            // 查询订单项列表（分页）
            List<OrderItem> orderItemList = orderItemService.listByMerchantIdPage(merchantId, pageNo, pageSize);
            // 统计订单项总数
            Integer total = orderItemService.countByMerchantId(merchantId);
            // 计算总页数
            Integer pages = total == 0 ? 0 : (total + pageSize - 1) / pageSize;

            // 关联商品名称
            List<OrderItemWithProductName> resultList = new ArrayList<>();
            for (OrderItem orderItem : orderItemList) {
                Product product = productService.getProductById(orderItem.getProductId());
                String productName = product == null ? "未知商品" : product.getProductName();
                resultList.add(new OrderItemWithProductName(orderItem, productName));
            }

            // 返回分页VO
            return new PageResultVO<>(total, pages, pageNo, pageSize, resultList);
        } catch (Exception e) {
            log.error("按商家ID{}分页查询订单项失败（pageNo={}，pageSize={}）", merchantId, pageNo, pageSize, e);
            return new PageResultVO<>(0, 0, pageNo, pageSize, Collections.emptyList());
        }
    }

    /**
     * 接口：按商家ID分页查询商品
     * 前端请求：GET /api/merchant/log/product/page?merchantId=xxx&pageNo=1&pageSize=10
     * 返回：分页结果（每页10条）
     */
    @GetMapping("/product/page")
    public PageResultVO<Product> getProductByMerchantIdPage(
            @RequestParam Long merchantId,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            log.info("按商家ID分页查询商品：merchantId={}，pageNo={}，pageSize={}", merchantId, pageNo, pageSize);
            return productService.getProductByMerchantIdPage(merchantId, pageNo, pageSize);
        } catch (Exception e) {
            log.error("按商家ID{}分页查询商品失败（pageNo={}，pageSize={}）", merchantId, pageNo, pageSize, e);
            return new PageResultVO<>(0, 0, pageNo, pageSize, Collections.emptyList());
        }
    }

    public static class OrderItemWithProductName {
        private OrderItem orderItem;
        private String productName;

        public OrderItemWithProductName(OrderItem orderItem, String productName) {
            this.orderItem = orderItem;
            this.productName = productName;
        }

        // Getter & Setter
        public OrderItem getOrderItem() {
            return orderItem;
        }

        public void setOrderItem(OrderItem orderItem) {
            this.orderItem = orderItem;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }
    }
}