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
         * 插入商品信息
         * 
         * @param product 商品实体
         * @return 插入结果，影响的行数
         */
        @Insert("INSERT INTO product (product_name, product_desc, stock, product_pic, product_price, merchant_id) " +
                        "VALUES (#{productName}, #{productDesc}, #{stock}, #{productPic}, #{productPrice}, #{merchantId})")
        @Options(useGeneratedKeys = true, keyProperty = "productId")
        int insertProduct(Product product);

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
