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
     * @param id 订单项目号
     * @return 订单项目主实体
     */
    @Select("SELECT * FROM order_item WHERE order_no = #{orderNo}")
    List<OrderItem> selectByOrderNo(String orderNo);

    /**
     * 插入订单项目信息
     * 
     * @param orderItem 订单项目实体
     * @return 插入结果，影响的行数
     */
    @Insert("INSERT INTO product (order_no, product_id, quantity, merchant_id) " +
            "VALUES (#{orderNo}, #{productId}, #{quantity}, #{merchantId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertOrderItem(OrderItem orderItem);
}
