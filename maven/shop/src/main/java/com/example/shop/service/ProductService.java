package com.example.shop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.shop.entity.Product;
import com.example.shop.mapper.ProductMapper;
import com.example.shop.vo.PageResultVO;

import lombok.extern.slf4j.Slf4j; // 新增日志依赖，便于排查问题
import java.util.Collections;
import java.util.List;

@Slf4j // 新增日志注解
@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    /**
     * 根据商品ID查询
     * 
     * @param productId 商品主键ID
     * @return 查询成功：商品实体
     *         查询失败：null
     */
    public Product getProductById(Long productId) {
        // ID不合法
        if (productId == null || productId <= 0) {
            return null;
        }
        return productMapper.selectByProductId(productId);
    }

    /**
     * 按名称模糊分页查找（完整分页）
     *
     * @param name     商品名称
     * @param pageNo   页数
     * @param pageSize 每页数量
     * @return 完整的分页结果（列表+总数）
     */
    public PageResultVO<Product> getProductByNamePage(String name, Integer pageNo, Integer pageSize) {
        if (pageSize == null) {
            pageSize = 1;
        }

        Integer offset = (pageNo - 1) * pageSize;

        List<Product> productList = productMapper.selectByNamePage(name, offset, pageSize);

        Integer total = productMapper.selectCountByName(name);

        Integer pages = (total + pageSize - 1) / pageSize;

        return new PageResultVO<Product>(total, pages, pageNo, pageSize, productList);
    }

    /**
     * 新增商品
     * 
     * @param product 商品实体
     * @return 插入成功：商品ID
     *         参数非法：-1
     *         插入失败：0
     */
    @Transactional(rollbackFor = Exception.class)
    public Long addProduct(Product product) {
        if (product == null) {
            return -1L;
        }
        if (productMapper.insertProduct(product) > 0) {
            return product.getProductId();
        }
        // 插入失败
        return 0L;
    }

    /**
     * 更新商品
     * 
     * @param product 商品实体
     * @return 更新成功:商品ID
     *         参数非法：-1
     *         更新失败：0
     */
    @Transactional(rollbackFor = Exception.class)
    public Long updateProduct(Product product) {
        if (product == null) {
            return -1L;
        }
        if (productMapper.updateProductByProductId(product) > 0) {
            return product.getProductId();
        }
        // 插入失败
        return 0L;
    }

    /**
     * 删除商品
     * 
     * @param productId 商品ID
     * @return 删除成功：商品ID
     *         参数非法：-1
     *         删除失败：0
     */
    @Transactional(rollbackFor = Exception.class)
    public Long deleteProduct(Long productId) {
        // ID不合法
        if (productId == null || productId <= 0) {
            return -1L;
        }

        if (productMapper.deleteProductByProductId(productId) > 0) {
            // 删除成功，返回被删除的ID
            return productId;
        }
        // 删除失败，返回0
        return 0L;
    }

    /**
     * 重构：更新商品库存（替代直接减库存，保留原方法名和返回值规则）
     * 
     * @param productId 商品ID
     * @param quantity  要扣减的数量（正数）
     * @return 更新成功：商品ID
     *         参数非法：-1
     *         更新失败：0（库存不足/商品不存在/并发修改）
     */
    @Transactional(rollbackFor = Exception.class)
    public Long decreaseStock(Long productId, Integer quantity) {
        // 1. 参数校验（保留原规则）
        if (productId == null || productId <= 0 || quantity == null || quantity <= 0) {
            log.warn("库存更新失败：参数非法（productId={}, quantity={}）", productId, quantity);
            return -1L;
        }

        // 2. 查询商品及当前库存
        Product product = productMapper.selectByProductId(productId);
        if (product == null) {
            log.error("库存更新失败：商品ID={}不存在", productId);
            return 0L;
        }
        Integer currentStock = product.getStock();

        // 3. 校验库存是否充足
        if (currentStock < quantity) {
            log.error("库存更新失败：商品ID={}库存不足（当前{}，需扣减{}）", productId, currentStock, quantity);
            return 0L;
        }

        // 4. 计算新库存
        Integer newStock = currentStock - quantity;

        // 5. 执行库存更新（带并发校验：仅当更新前库存未变时生效）
        int updateCount = productMapper.updateStockByProductId(productId, newStock, currentStock);
        if (updateCount == 0) {
            log.warn("库存更新失败：商品ID={}库存已被其他请求修改（更新前库存：{}）", productId, currentStock);
            return 0L;
        }

        // 6. 更新成功，返回商品ID（保留原返回规则）
        log.info("商品ID={}库存更新成功：原{} → 新{}", productId, currentStock, newStock);
        return productId;
    }

    /**
     * 按商家ID分页查询商品（每页默认10条）
     *
     * @param merchantId 商家ID
     * @param pageNo     页数（从1开始）
     * @param pageSize   每页数量（默认10）
     * @return 完整分页结果（列表+总数）
     */
    public PageResultVO<Product> getProductByMerchantIdPage(Long merchantId, Integer pageNo, Integer pageSize) {
        // 1. 参数校验 & 兜底
        if (merchantId == null || merchantId <= 0) {
            return new PageResultVO<>(0, 0, pageNo, pageSize, Collections.emptyList());
        }
        if (pageNo == null || pageNo < 1) {
            pageNo = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10; // 默认每页10条
        }

        // 2. 计算偏移量
        Integer offset = (pageNo - 1) * pageSize;

        // 3. 查询列表 + 总数
        List<Product> productList = productMapper.selectByMerchantIdPage(merchantId, offset, pageSize);
        Integer total = productMapper.selectCountByMerchantId(merchantId);

        // 4. 计算总页数
        Integer pages = total == 0 ? 0 : (total + pageSize - 1) / pageSize;

        // 5. 返回统一分页VO
        return new PageResultVO<>(total, pages, pageNo, pageSize, productList);
    }
}