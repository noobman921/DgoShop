package com.example.shop.vo;

import com.example.shop.entity.OrderItem;
import com.example.shop.entity.OrderMain;
import lombok.Data;

import java.util.List;

/**
 * 订单详情VO（对应购物车项VO的结构，单条订单数据）
 */
@Data
public class OrderDetailVO {
    // 订单主信息（对应购物车项的主信息）
    private OrderMain orderMain;
    // 该订单下的所有商品项（对应购物车项的商品列表）
    private List<OrderItem> orderItemList;
}