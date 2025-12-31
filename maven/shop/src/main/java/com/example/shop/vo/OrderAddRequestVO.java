package com.example.shop.vo;

import com.example.shop.entity.OrderItem;
import lombok.Data;

import java.util.List;

/**
 * 新增订单请求VO（商家ID仅在orderItemList中）
 */
@Data
public class OrderAddRequestVO {
    private String userAccount; // 用户账号
    private List<OrderItem> orderItemList; // 订单项目列表
}