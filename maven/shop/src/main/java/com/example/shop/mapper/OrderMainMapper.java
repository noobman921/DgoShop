package com.example.shop.mapper;

import com.example.shop.entity.OrderMain;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface OrderMainMapper {
    /**
     * 根据订单号查询（主键查询）
     * 
     * @param orderNo 订单号
     * @return 订单主实体
     */
    @Select("SELECT * FROM order_main WHERE order_no = #{orderNo}")
    OrderMain selectByOrderNO(String orderNo);

    /**
     * 根据用户ID查询
     * 
     * @param userId 用户主键ID
     * @return 订单主实体
     */
    @Select("SELECT * FROM order_main WHERE user_id = #{userId}")
    List<OrderMain> selectByUserId(Long userId);

    /**
     * 插入订单主实体
     * 
     * @param orderMain 订单主实体
     * @return 插入结果，影响的行数
     */
    @Insert("INSERT INTO order_main (order_no, user_id) " +
            "VALUES (orderNo, userId)")
    int insertOrderMain(OrderMain orderMain);
}
