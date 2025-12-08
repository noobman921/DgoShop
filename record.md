# 数据库构思
商品表：商品ID 商品名 商品描述 库存量 商品图片 商品价格 商家ID
商家表：商家ID 商家名 账号 密码
用户表：用户ID 用户名 账号 密码
订单主表：订单号 用户ID
订单表：ID 订单号 商品ID 商品数量 用户ID 商家ID
购物车表：用户ID 商品ID 商品数量

1. 用户/商家注册：将账号/密码等信息存入用户表/商家表；
2. 商家添加商品：将商品信息（名/价格/库存等）存入商品表（关联商家ID）；
3. 用户操作购物车：
   - 加购：向购物车表插入（用户ID+商品ID+数量）；
   - 修改数量：更新购物车表对应记录的数量；
   - 删除：删除购物车表对应记录；
4. 用户下单：
   - 第一步：生成唯一订单号，向订单主表插入（订单号+用户ID）；
   - 第二步：将购物车中选中的商品逐条插入订单详情表（订单号+商品ID+数量+商家ID）；
   - 第三步：可选（基础功能）：清空购物车对应商品/扣减商品表库存；
5. 订单查询：
   - 按用户ID查订单主表，获取该用户所有订单号；
   - 按订单号查订单详情表，获取该订单下所有商品信息。


package com.example.shop.mapper;

import com.example.shop.entity.Product;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品Mapper接口（仅处理product表的增删改查，一表一Mapper）
 * 注解版 + XML版混合示例（简单SQL用注解，复杂SQL用XML）
 */
@Mapper // 告诉Spring这是MyBatis Mapper接口，无需手动扫描
@Repository // 标识为持久层组件，避免IDE注入警告
public interface ProductMapper {

    // ==================== 基础单表操作（注解版，简单SQL） ====================
    /**
     * 根据商品ID查询（主键查询）
     * @param productId 商品主键ID
     * @return 商品实体
     */
    @Select("SELECT * FROM product WHERE product_id = #{productId}")
    Product selectByProductId(Long productId);

    /**
     * 新增商品（主键自增，无需传入productId）
     * @param product 商品实体（含name/desc/stock/pic/price/merchantId）
     * @return 受影响行数
     */
    @Insert("INSERT INTO product (product_name, product_desc, stock, product_pic, product_price, merchant_id) " +
            "VALUES (#{productName}, #{productDesc}, #{stock}, #{productPic}, #{productPrice}, #{merchantId})")
    int insertProduct(Product product);

    /**
     * 根据商品ID更新库存（高频业务操作）
     * @param productId 商品ID
     * @param stock 新库存数
     * @return 受影响行数
     */
    @Update("UPDATE product SET stock = #{stock} WHERE product_id = #{productId}")
    int updateStockById(@Param("productId") Long productId, @Param("stock") Integer stock);

    /**
     * 根据商品ID删除（逻辑删除场景可改为更新状态，物理删除慎用）
     * @param productId 商品ID
     * @return 受影响行数
     */
    @Delete("DELETE FROM product WHERE product_id = #{productId}")
    int deleteByProductId(Long productId);

    // ==================== 复杂查询（XML版，动态SQL） ====================
    /**
     * 多条件查询商品（商户ID+价格区间+库存>0）
     * 复杂动态SQL建议写在XML中，可读性更高
     * @param merchantId 商户ID
     * @param minPrice 最低价格
     * @param maxPrice 最高价格
     * @return 商品列表
     */
    List<Product> selectByCondition(
            @Param("merchantId") Long merchantId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );

    /**
     * 批量扣减库存（电商核心场景：下单时扣库存）
     * @param productIds 商品ID列表
     * @param quantities 扣减数量列表（与商品ID一一对应）
     * @return 受影响行数
     */
    int batchReduceStock(
            @Param("productIds") List<Long> productIds,
            @Param("quantities") List<Integer> quantities
    );
}

<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" 
"http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<!-- namespace 必须对应Mapper接口全类名 -->
<mapper namespace="com.example.shop.mapper.ProductMapper">

    <!-- 通用结果映射（复用，避免重复写字段映射） -->
    <resultMap id="ProductResultMap" type="com.example.shop.entity.Product">
        <id column="product_id" property="productId"/> <!-- 主键映射 -->
        <result column="product_name" property="productName"/>
        <result column="product_desc" property="productDesc"/>
        <result column="stock" property="stock"/>
        <result column="product_pic" property="productPic"/>
        <result column="product_price" property="productPrice"/>
        <result column="merchant_id" property="merchantId"/>
    </resultMap>

    <!-- 实现selectByCondition方法（动态条件查询） -->
    <select id="selectByCondition" resultMap="ProductResultMap">
        SELECT * FROM product
        <where>
            <!-- 商户ID非空则过滤 -->
            <if test="merchantId != null">
                AND merchant_id = #{merchantId}
            </if>
            <!-- 价格区间过滤 -->
            <if test="minPrice != null">
                AND product_price >= #{minPrice}
            </if>
            <if test="maxPrice != null">
                AND product_price <= #{maxPrice}
            </if>
            <!-- 库存>0（只查可售商品） -->
            AND stock > 0
        </where>
        <!-- 按价格降序排序 -->
        ORDER BY product_price DESC
    </select>

    <!-- 实现batchReduceStock方法（批量扣库存） -->
    <update id="batchReduceStock">
        UPDATE product
        SET stock = stock - CASE
            <foreach collection="productIds" item="productId" index="index">
                WHEN product_id = #{productId} THEN #{quantities[index]}
            </foreach>
        END
        WHERE product_id IN
            <foreach collection="productIds" item="productId" open="(" separator="," close=")">
                #{productId}
            </foreach>
            AND stock >= 
            <foreach collection="quantities" item="quantity" index="index" open="CASE product_id" separator=" " close="END">
                WHEN #{productIds[index]} THEN #{quantity}
            </foreach>
    </update>

</mapper>