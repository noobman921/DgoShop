package com.example.shop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.example.shop.entity.Product;
import com.example.shop.service.ProductService;

import com.example.shop.vo.Result;
import com.example.shop.vo.PageResultVO;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 根据商品ID查找
     * GET /api/product/productId/{productId}
     */
    @GetMapping("/productId/{productId}")
    public Result<Product> getProductById(@PathVariable Long productId) {
        Product product = productService.getProductById(productId);
        return Result.success(product);
    }

    /**
     * 根据商品名称模糊查找
     * GET /api/product/productName/{productName}/{pageOffset}/{pageNum}
     */
    @GetMapping("/productName")
    public Result<PageResultVO<Product>> getProductByName(
            @RequestParam(required = false) String productName,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        // 参数合法性校验
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }
        // 调用服务层获取分页结果
        PageResultVO<Product> pageResult = productService.getProductByNamePage(productName, pageNum, pageSize);
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
