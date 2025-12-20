package com.example.shop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.shop.entity.Product;

import com.example.shop.mapper.ProductMapper;
import com.example.shop.vo.PageResultVO;

import java.util.List;

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
