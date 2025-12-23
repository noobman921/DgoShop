package com.example.shop.vo;

import com.example.shop.entity.OrderMain;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单详情VO（适配合并后的订单项VO）
 */
@Data
public class OrderDetailVO {
    private OrderMain orderMain; // 订单主信息
    private List<OrderItemVO> itemList; // 合并后的订单项列表
    private BigDecimal totalPrice; // 订单总价格
}