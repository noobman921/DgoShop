package com.example.shop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.shop.entity.Product;

import com.example.shop.mapper.ProductMapper;

@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    /**
     * 根据商品ID查询
     * 
     * @param productId 商品主键ID
     * @return 商品实体
     */
    public Product getProductById(Long productId) {
        // ID不合法
        if (productId == null || productId <= 0) {
            return null;
        }
        return productMapper.selectByProductId(productId);
    }

    /**
     * 新增商品
     * 
     * @param product 商品实体
     * @return 插入成功：商品ID
     *         插入失败：null
     */
    @Transactional(rollbackFor = Exception.class)
    public Long addProduct(Product product) {
        if (product == null) {
            return null;
        }
        if (productMapper.insertProduct(product) > 0) {
            return product.getProductId();
        }
        // 插入失败
        return null;
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
     * 更新商品库存
     * 
     * @param productId 商品ID
     * @param quantity  更新数量
     * @return 更新成功：商品ID
     *         参数非法：-1
     *         更新失败：0
     */
    @Transactional(rollbackFor = Exception.class)
    public Long decreaseStock(Long productId, Integer quantity) {
        // 检验参数
        if (productId == null || productId <= 0 || quantity == null) {
            return -1L;
        }
        // 更新
        if (productMapper.decreaseStockByProductId(productId, quantity) > 0) {
            return productId;
        }
        // 操作失败
        return 0L;
    }
}
