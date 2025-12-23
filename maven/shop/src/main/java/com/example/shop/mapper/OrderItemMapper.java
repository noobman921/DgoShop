package com.example.shop.mapper;

import com.example.shop.entity.OrderItem;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface OrderItemMapper {
    /**
     * 根据订单项目号查询（主键查询）
     * 
     * @param id 订单项目号
     * @return 订单项目主实体
     */
    @Select("SELECT * FROM order_item WHERE id = #{id}")
    OrderItem selectById(Long id);

    /**
     * 根据订单号查询
     * 
     * @param id 订单号
     * @return 订单项目主实体
     */
    @Select("SELECT * FROM order_item WHERE order_no = #{orderNo}")
    List<OrderItem> selectByOrderNo(String orderNo);

    /**
     * 按商家ID分页查找订单项
     *
     * @param merchantId 商家ID
     * @param offset     偏移量（从0开始，offset = (页码-1)*每页数量）
     * @param limit      每页数量
     * @return 该商家下的订单项分页列表
     */
    @Select("SELECT * FROM order_item WHERE merchant_id = #{merchantId} LIMIT #{offset}, #{limit}")
    List<OrderItem> selectByMerchantIdPage(
            @Param("merchantId") Long merchantId,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit);

    /**
     * 统计商家ID对应的订单项总数（分页配套方法）
     *
     * @param merchantId 商家ID
     * @return 该商家下的订单项总数
     */
    @Select("SELECT COUNT(*) FROM order_item WHERE merchant_id = #{merchantId}")
    Integer countByMerchantId(@Param("merchantId") Long merchantId);

    /**
     * 插入订单项目信息
     * 
     * @param orderItem 订单项目实体
     * @return 插入结果，影响的行数
     */
    @Insert("INSERT INTO order_item (order_no, product_id, quantity, merchant_id) " +
            "VALUES (#{orderNo}, #{productId}, #{quantity}, #{merchantId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertOrderItem(OrderItem orderItem);
}
