package com.example.shop.controller;

import com.example.shop.entity.Merchant;
import com.example.shop.entity.OrderItem;
import com.example.shop.entity.Product;
import com.example.shop.service.MerchantService;
import com.example.shop.service.OrderItemService;
import com.example.shop.service.ProductService;
import com.example.shop.vo.PageResultVO;
import com.example.shop.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 商家专属Controller
 * 风格对齐UserController：GET请求+RequestParam传参+Result响应体
 * 提供：1.商家注册 2.商家登录 3.按商家ID分页查订单（含商品名称）；4.按商家ID分页查商品；5.更新商品库存
 */
@RestController
@RequestMapping("/api/merchant/log")
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private ProductService productService;

    // ========== 原有所有方法完全保留（登录/注册/查订单/查商品） ==========
    @GetMapping("/login")
    public Result<Merchant> login(
            @RequestParam("account") String account,
            @RequestParam("password") String password) {
        if (!StringUtils.hasText(account)) {
            return Result.fail("账号不能为空");
        }
        if (!StringUtils.hasText(password)) {
            return Result.fail("密码不能为空");
        }
        Merchant merchant = merchantService.getMerchantByAccount(account);
        if (merchant == null) {
            return Result.fail("账号不存在");
        }
        if (!password.equals(merchant.getPassword())) {
            return Result.fail("密码错误");
        }
        return Result.success(merchant);
    }

    @GetMapping("/reg")
    public Result<String> register(
            @RequestParam("merchantName") String merchantName,
            @RequestParam("account") String account,
            @RequestParam("password") String password) {
        if (!StringUtils.hasText(merchantName)) {
            return Result.fail("商家名称不能为空");
        }
        if (!StringUtils.hasText(account)) {
            return Result.fail("账号不能为空");
        }
        if (!StringUtils.hasText(password)) {
            return Result.fail("密码不能为空");
        }
        Merchant existMerchant = merchantService.getMerchantByAccount(account);
        if (existMerchant != null) {
            return Result.fail("账号已存在");
        }
        Merchant merchant = new Merchant();
        merchant.setMerchantName(merchantName);
        merchant.setAccount(account);
        merchant.setPassword(password);
        boolean registerSuccess = merchantService.addMerchant(merchant);
        if (registerSuccess) {
            return Result.success("注册成功");
        } else {
            return Result.fail("注册失败，请重试");
        }
    }

    @GetMapping("/order-item/page")
    public PageResultVO<OrderItemWithProductName> getOrderItemByMerchantIdPage(
            @RequestParam Long merchantId,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            if (merchantId == null || merchantId <= 0) {
                return new PageResultVO<>(0, 0, pageNo, pageSize, Collections.emptyList());
            }
            List<OrderItem> orderItemList = orderItemService.listByMerchantIdPage(merchantId, pageNo, pageSize);
            Integer total = orderItemService.countByMerchantId(merchantId);
            Integer pages = total == 0 ? 0 : (total + pageSize - 1) / pageSize;
            List<OrderItemWithProductName> resultList = new ArrayList<>();
            for (OrderItem orderItem : orderItemList) {
                Product product = productService.getProductById(orderItem.getProductId());
                String productName = product == null ? "未知商品" : product.getProductName();
                resultList.add(new OrderItemWithProductName(orderItem, productName));
            }
            return new PageResultVO<>(total, pages, pageNo, pageSize, resultList);
        } catch (Exception e) {
            return new PageResultVO<>(0, 0, pageNo, pageSize, Collections.emptyList());
        }
    }

    @GetMapping("/product/page")
    public PageResultVO<Product> getProductByMerchantIdPage(
            @RequestParam Long merchantId,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            return productService.getProductByMerchantIdPage(merchantId, pageNo, pageSize);
        } catch (Exception e) {
            return new PageResultVO<>(0, 0, pageNo, pageSize, Collections.emptyList());
        }
    }

    // ========== 新增：库存更新接口（适配前端PUT请求 + JSON参数） ==========
    @PutMapping("/product/updateStock") // 匹配前端PUT请求
    public Result<String> updateProductStock(@RequestBody StockUpdateDTO dto) {
        // 极简参数校验（无安全措施，仅基础判空）
        if (dto.getProductId() == null || dto.getProductId() <= 0) {
            return Result.fail("商品ID不能为空");
        }
        if (dto.getStock() == null || dto.getStock() < 0) {
            return Result.fail("库存数量不能为负数");
        }

        // 查询商品并构造更新对象
        Product product = productService.getProductById(dto.getProductId());
        if (product == null) {
            return Result.fail("商品不存在");
        }
        product.setStock(dto.getStock()); // 仅更新库存字段

        // 调用原有updateProduct方法更新
        Long result = productService.updateProduct(product);
        if (result.equals(dto.getProductId())) {
            return Result.success("库存更新成功");
        } else {
            return Result.fail("库存更新失败");
        }
    }

    // 内部静态类：接收前端库存更新参数（匹配前端JSON字段）
    public static class StockUpdateDTO {
        private Long productId; // 匹配前端productId
        private Integer stock; // 匹配前端stock（对应newStock）

        // Getter & Setter
        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getStock() {
            return stock;
        }

        public void setStock(Integer stock) {
            this.stock = stock;
        }
    }

    public static class OrderItemWithProductName {
        private OrderItem orderItem;
        private String productName;

        public OrderItemWithProductName(OrderItem orderItem, String productName) {
            this.orderItem = orderItem;
            this.productName = productName;
        }

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