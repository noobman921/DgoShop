package com.example.shop.mapper;

import com.example.shop.entity.Product;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface ProductMapper {
        /**
         * 根据商品ID查询（主键查询）
         * 
         * @param productId 商品主键ID
         * @return 商品实体
         */
        @Select("SELECT * FROM product WHERE product_id = #{productId}")
        Product selectByProductId(Long productId);

        /**
         * 按名称模糊查找总记录数
         *
         * @param name 商品名称（模糊匹配）
         * @return 符合条件的商品总数
         */
        @Select("SELECT COUNT(*) FROM product WHERE product_name LIKE CONCAT('%', #{name}, '%') AND is_on_shelf = 1")
        Integer selectCountByName(@Param("name") String name);

        /**
         * 按商家ID统计商品总数
         *
         * @param merchantId 商家ID
         * @return 该商家下的商品总数
         */
        @Select("SELECT COUNT(*) FROM product WHERE merchant_id = #{merchantId}")
        Integer selectCountByMerchantId(@Param("merchantId") Long merchantId);

        /**
         * 插入商品信息
         * 
         * @param product 商品实体
         * @return 插入结果，影响的行数
         */
        @Insert("INSERT INTO product (product_name, product_desc, stock, product_pic, product_price, merchant_id) " +
                        "VALUES (#{productName}, #{productDesc}, #{stock}, #{productPic}, #{productPrice}, #{merchantId})")
        @Options(useGeneratedKeys = true, keyProperty = "productId")
        int insertProduct(Product product);

        /**
         * 更新商品信息
         *
         * @param product 商品实体（含productId及待更新字段）
         * @return 影响行数
         */
        @Update("UPDATE product SET product_name = #{productName}, product_price = #{productPrice}, " +
                        "stock = #{stock}, product_pic = #{productPic}, is_on_shelf = #{isOnShelf} WHERE product_id = #{productId}")
        int updateProductByProductId(Product product);

        /**
         * 删除商品信息
         *
         * @param productId 商品ID
         * @return 影响行数
         */
        @Delete("DELETE FROM product WHERE product_id = #{productId}")
        int deleteProductByProductId(Long productId);

        /**
         * 减少商品库存（保留原有方法，新增下方「更新库存」方法）
         *
         * @param productId 商品ID
         * @param quantity  减少的数量
         * @return 影响行数
         */
        @Update("UPDATE product SET stock = stock - #{quantity} WHERE product_id = #{productId} AND stock >= #{quantity}")
        int decreaseStockByProductId(Long productId, Integer quantity);

        /**
         * 更新商品库存（直接设置库存值，带乐观锁校验防超卖）
         * 
         * @param productId 商品ID
         * @param newStock  新库存值（计算后的值：当前库存 - 购买数量）
         * @param oldStock  更新前的库存值（用于校验，防止并发修改）
         * @return 影响行数（1=成功，0=库存已被修改/不足）
         */
        @Update("UPDATE product SET stock = #{newStock} " +
                        "WHERE product_id = #{productId} AND stock = #{oldStock} AND #{newStock} >= 0")
        int updateStockByProductId(
                        @Param("productId") Long productId,
                        @Param("newStock") Integer newStock,
                        @Param("oldStock") Integer oldStock);

        // ===== 批量操作 =====
        /**
         * 按商家ID分页查找商品
         *
         * @param merchantId 商家ID
         * @param offset     偏移量
         * @param limit      每页数量
         * @return 商品分页列表
         */
        @Select("SELECT * FROM product WHERE merchant_id = #{merchantId} LIMIT #{offset}, #{limit}")
        List<Product> selectByMerchantIdPage(
                        @Param("merchantId") Long merchantId,
                        @Param("offset") Integer offset,
                        @Param("limit") Integer limit);

        /**
         * 按名称模糊分页查找商品
         *
         * @param name   商品名称（模糊匹配）
         * @param offset 偏移量
         * @param limit  每页数量
         * @return 商品分页列表
         */
        @Select("SELECT * FROM product WHERE product_name LIKE CONCAT('%', #{name}, '%') AND is_on_shelf = 1 LIMIT #{offset}, #{limit} ")
        List<Product> selectByNamePage(
                        @Param("name") String name,
                        @Param("offset") Integer offset,
                        @Param("limit") Integer limit);
}