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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 商家专属Controller
 * 风格对齐UserController：GET请求+RequestParam传参+Result响应体
 * 提供：1.商家注册 2.商家登录 3.按商家ID分页查订单（含商品名称）；4.按商家ID分页查商品；5.更新商品库存；6.新增商品（含图片上传）
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

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    // ========== 新增：注入图片上传路径（从application.yml读取） ==========
    @Value("${upload.path:/opt/shop_uploads/products/}") // 默认路径，防止配置缺失
    private String uploadPath;

    // ========== 原有所有方法完全保留（登录/注册/查订单/查商品/更新库存） ==========
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

    @PutMapping("/product/changeShelfStatus")
    public Result<String> changeShelfStatus(
            @RequestParam Long productId,
            @RequestParam Integer isOnShelf) {
        // 参数校验
        if (productId == null || productId <= 0) {
            return Result.fail("商品ID不能为空");
        }
        if (isOnShelf != 0 && isOnShelf != 1) {
            return Result.fail("上架标识只能是0（下架）或1（上架）");
        }

        // 查询商品并更新状态
        Product product = productService.getProductById(productId);
        if (product == null) {
            return Result.fail("商品不存在");
        }
        product.setIsOnShelf(isOnShelf);
        Long result = productService.updateProduct(product);

        if (result.equals(productId)) {
            String msg = isOnShelf == 1 ? "商品上架成功" : "商品下架成功";
            return Result.success(msg);
        } else {
            return Result.fail("修改上下架状态失败");
        }
    }

    @PutMapping("/product/updateStock")
    public Result<String> updateProductStock(@RequestBody StockUpdateDTO dto) {
        if (dto.getProductId() == null || dto.getProductId() <= 0) {
            return Result.fail("商品ID不能为空");
        }
        if (dto.getStock() == null || dto.getStock() < 0) {
            return Result.fail("库存数量不能为负数");
        }

        Product product = productService.getProductById(dto.getProductId());
        if (product == null) {
            return Result.fail("商品不存在");
        }
        product.setStock(dto.getStock());
        Long result = productService.updateProduct(product);
        if (result.equals(dto.getProductId())) {
            return Result.success("库存更新成功");
        } else {
            return Result.fail("库存更新失败");
        }
    }

    @RequestMapping("/product/add")
    public Result<Product> addProduct(
            // 商品基本信息（必填）
            @RequestParam("productName") String productName,
            @RequestParam("productPrice") BigDecimal productPrice,
            @RequestParam("stock") Integer stock,
            @RequestParam("merchantId") Long merchantId,
            // 商品基本信息（可选）
            @RequestParam(value = "productDesc", required = false) String productDesc,
            // 商品图片（可选，MultipartFile接收文件）
            @RequestParam(value = "file", required = false) MultipartFile file) {

        // ========== 1. 打印入参（第一步：确认参数是否接收成功） ==========
        log.info("===== 开始执行添加商品接口 =====");
        log.info("接收的入参：productName={}, productPrice={}, stock={}, merchantId={}, productDesc={}",
                productName, productPrice, stock, merchantId, productDesc);
        // 打印文件核心信息（是否为空、文件名、大小）
        if (file == null) {
            log.info("接收的文件：file=null（未上传图片）");
        } else {
            log.info("接收的文件：fileName={}, fileSize={}KB, isEmpty={}",
                    file.getOriginalFilename(), file.getSize() / 1024, file.isEmpty());
        }

        // ========== 2. 参数校验 ==========
        log.info("===== 开始参数校验 =====");
        if (!StringUtils.hasText(productName)) {
            log.error("参数校验失败：商品名称不能为空");
            return Result.fail("商品名称不能为空");
        }
        if (productPrice == null || productPrice.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("参数校验失败：商品价格必须大于0，当前值={}", productPrice);
            return Result.fail("商品价格必须大于0");
        }
        if (stock == null || stock < 0) {
            log.error("参数校验失败：库存数量不能为负数，当前值={}", stock);
            return Result.fail("库存数量不能为负数");
        }
        if (merchantId == null || merchantId <= 0) {
            log.error("参数校验失败：商家ID不能为空且必须大于0，当前值={}", merchantId);
            return Result.fail("商家ID不能为空且必须大于0");
        }
        log.info("参数校验通过");

        // ========== 3. 处理图片上传（可选） ==========
        String productPic = ""; // 商品图片相对路径（存入数据库）
        if (file != null && !file.isEmpty()) {
            log.info("===== 开始处理图片上传 =====");
            try {
                // 3.1 校验图片格式（仅允许jpg/png/jpeg/webp）
                String originalFilename = file.getOriginalFilename();
                log.info("原始文件名：{}", originalFilename);
                String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
                log.info("文件后缀：{}", suffix);

                if (!suffix.equalsIgnoreCase(".jpg") && !suffix.equalsIgnoreCase(".png")
                        && !suffix.equalsIgnoreCase(".jpeg") && !suffix.equalsIgnoreCase(".webp")) {
                    log.error("图片格式校验失败：仅支持jpg/png/jpeg/webp，当前后缀={}", suffix);
                    return Result.fail("仅支持jpg/png/jpeg/webp格式的图片！");
                }
                log.info("图片格式校验通过");

                // 3.2 生成唯一文件名（时间戳 + 随机数 + 后缀，避免重复）
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String fileName = sdf.format(new Date()) + "_" + UUID.randomUUID().toString().substring(0, 6) + suffix;
                log.info("生成唯一文件名：{}", fileName);

                // 3.3 创建存储目录（如果不存在）
                File destDir = new File(uploadPath);
                log.info("图片存储目录：{}，目录是否存在：{}", uploadPath, destDir.exists());
                if (!destDir.exists()) {
                    boolean mkdirsResult = destDir.mkdirs(); // 递归创建目录
                    log.info("目录不存在，执行创建，创建结果：{}", mkdirsResult);
                    if (!mkdirsResult) {
                        log.error("目录创建失败：{}", uploadPath);
                        return Result.fail("图片存储目录创建失败，请检查路径权限");
                    }
                }

                // 3.4 保存图片到本地磁盘
                File destFile = new File(uploadPath + fileName);
                log.info("目标文件完整路径：{}，文件是否已存在：{}", destFile.getAbsolutePath(), destFile.exists());
                file.transferTo(destFile);
                log.info("图片保存成功，保存路径：{}", destFile.getAbsolutePath());

                // 3.5 生成图片相对路径（前端可通过 http://localhost:8080 + 路径 访问）
                productPic = "/uploads/products/" + fileName;
                log.info("生成图片相对路径：{}", productPic);

            } catch (IOException e) {
                // 打印完整异常堆栈（不止getMessage，能看到具体错误行）
                log.error("图片上传失败！", e);
                return Result.fail("图片上传失败：" + e.getMessage());
            }
        } else {
            log.info("未上传图片，跳过图片处理流程");
        }

        // ========== 4. 构造商品对象并保存 ==========
        log.info("===== 开始构造商品对象并保存 =====");
        Product product = new Product();
        product.setProductName(productName);
        product.setProductDesc(StringUtils.hasText(productDesc) ? productDesc : ""); // 描述为空则设空字符串
        product.setStock(stock);
        product.setProductPic(productPic); // 存入图片相对路径
        product.setProductPrice(productPrice);
        product.setMerchantId(merchantId);
        product.setIsOnShelf(1);
        log.info("构造的商品对象：{}", product.toString()); // 确保Product类重写了toString()

        // 调用service保存商品
        log.info("调用productService.addProduct()保存商品");
        long addSuccess = productService.addProduct(product);
        log.info("商品保存结果：addSuccess={}", addSuccess);

        if (addSuccess > 0) {
            log.info("商品添加成功，返回商品对象：{}", product);
            return Result.success(product);
        } else {
            log.error("商品添加失败，保存结果addSuccess={}", addSuccess);
            return Result.fail("商品添加失败，请重试");
        }
    }

    // ========== 原有内部类保留 ==========
    public static class StockUpdateDTO {
        private Long productId;
        private Integer stock;

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