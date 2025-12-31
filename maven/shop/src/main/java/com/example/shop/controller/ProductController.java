package com.example.shop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // 新增：读取配置文件的upload.path
import org.springframework.core.io.FileSystemResource; // 新增：处理本地文件
import org.springframework.http.HttpHeaders; // 新增：设置响应头
import org.springframework.http.HttpStatus; // 新增：响应状态码
import org.springframework.http.MediaType; // 新增：设置图片媒体类型
import org.springframework.http.ResponseEntity; // 新增：返回文件流
import org.springframework.web.bind.annotation.*;

import com.example.shop.entity.Product;
import com.example.shop.service.ProductService;

import com.example.shop.vo.Result;
import com.example.shop.vo.PageResultVO;

import java.io.File; // 新增：文件操作
import java.io.IOException; // 新增：异常处理

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    // 新增：读取配置文件中的图片上传路径（和你application.properties中的upload.path对应）
    @Value("${upload.path}")
    private String uploadPath;

    /**
     * 新增：图片访问接口
     * GET /api/product/img/products/{fileName} → 返回图片二进制流
     */
    @GetMapping("/img/products/{fileName}")
    public ResponseEntity<FileSystemResource> getProductImage(@PathVariable String fileName) {
        try {
            // 拼接完整的图片文件路径（upload.path = /opt/shop_uploads/products/）
            File imageFile = new File(uploadPath + fileName);

            // 校验文件是否存在
            if (!imageFile.exists()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            // 设置响应头：指定图片类型、文件大小，避免浏览器解析错误
            HttpHeaders headers = new HttpHeaders();
            // 兼容jpg/png/webp（根据文件后缀自动判断，更通用）
            String fileType = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            switch (fileType) {
                case "png":
                    headers.setContentType(MediaType.IMAGE_PNG);
                    break;
                case "webp":
                    headers.setContentType(MediaType.valueOf("image/webp"));
                    break;
                default: // jpg/jpeg
                    headers.setContentType(MediaType.IMAGE_JPEG);
                    break;
            }
            headers.setContentLength(imageFile.length());

            // 返回图片文件流
            return new ResponseEntity<>(new FileSystemResource(imageFile), headers, HttpStatus.OK);
        } catch (Exception e) {
            // 捕获所有异常，返回500
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据商品ID查找
     * GET /api/product/productId
     */
    @GetMapping("/productId")
    public Result<Product> getProductById(@RequestParam Long productId) {
        Product product = productService.getProductById(productId);
        return Result.success(product);
    }

    /**
     * 根据商品名称模糊查找
     * GET /api/product/productName/
     */
    @GetMapping("/productName")
    public Result<PageResultVO<Product>> getProductByName(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "8") Integer pageSize) {
        // 参数合法性校验
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }
        // 调用服务层获取分页结果
        PageResultVO<Product> pageResult = productService.getProductByNamePage(name, pageNum, pageSize);
        // 返回全局统一响应
        return Result.success(pageResult);
    }

    /**
     * 增加商品
     * POST /api/product/add
     */
    @PostMapping("/add")
    public Result<?> addProduct(@RequestBody Product product) {
        Long result = productService.addProduct(product);
        // 根据Service返回值判断：>0=成功，否则失败
        return result > 0 ? Result.success("新增商品成功") : Result.fail("新增商品失败");
    }

    /**
     * 修改商品
     * PUT /api/product/update
     */
    @PutMapping("/update") // 匹配 PUT /api/product/update
    public Result<?> updateProduct(@RequestBody Product product) {
        Long result = productService.updateProduct(product);
        return result > 0 ? Result.success("修改商品成功") : Result.fail("修改商品失败");
    }

    /**
     * 删除商品
     * DELETE /api/product/{productId}
     */
    @DeleteMapping("/{productId}") // 匹配 DELETE /api/product/1
    public Result<?> deleteProduct(@PathVariable Long productId) {
        Long result = productService.deleteProduct(productId);
        return result > 0 ? Result.success("删除商品成功") : Result.fail("删除商品失败");
    }

    /**
     * 扣减库存（供购物车/下单调用）
     * POST /api/product/decrease-stock
     */
    @PostMapping("/decrease-stock") // 匹配 POST /api/product/decrease-stock
    public Result<?> decreaseStock(@RequestParam Long productId, @RequestParam Integer quantity) {
        // @RequestParam：获取URL参数（如?productId=1&quantity=2）
        Long result = productService.decreaseStock(productId, quantity);
        return result > 0 ? Result.success("扣减库存成功") : Result.fail("扣减库存失败（库存不足/参数非法）");
    }
}