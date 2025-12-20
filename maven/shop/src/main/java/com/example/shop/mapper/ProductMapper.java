package com.example.shop.mapper;

import com.example.shop.entity.Product;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

// import java.math.BigDecimal;
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
        @Select("SELECT COUNT(*) FROM product WHERE name LIKE CONCAT('%', #{name}, '%')")
        Integer selectCountByName(@Param("name") String name);

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
         * @param productId 商品ID
         * @param quantity  新数量（减少）
         * @return 影响行数
         */
        @Update("UPDATE product SET product_name = #{productName}, product_price = #{productPrice}, " +
                        "stock = #{stock}, product_pic = #{productPic} WHERE product_id = #{productId}")
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
         * 减少商品库存
         *
         * @param productId 商品ID
         * @param quantity  新数量（减少）
         * @return 影响行数
         */
        @Update("UPDATE product SET stock = stock - #{quantity} WHERE product_id = #{productId} AND stock >= #{quantity}")
        int decreaseStockByProductId(Long productId, Integer quantity);

        // ===== 批量操作 =====
        /**
         * 按商家ID查找
         *
         * @param merchantId 商家ID
         * @param offset     偏移量
         * @param limit      每页数量
         * @return 商品页
         */
        List<Product> selectByMerchantIdPage(
                        @Param("merchantId") Long merchantId,
                        @Param("offset") Integer offset,
                        @Param("limit") Integer limit);

        /**
         * 按名称模糊查找页
         *
         * @param merchantId 商家ID
         * @param offset     偏移量
         * @param limit      每页数量
         * @return 商品页
         */
        List<Product> selectByNamePage(
                        @Param("name") String name,
                        @Param("offset") Integer offset,
                        @Param("limit") Integer limit);

}
