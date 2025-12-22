package com.example.shop.vo;

import com.example.shop.entity.OrderItem;
import lombok.Data;

import java.util.List;

/**
 * 新增订单请求VO（商家ID仅在orderItemList中）
 */
@Data
public class OrderAddRequestVO {
    private String userAccount; // 用户账号（必传）
    private List<OrderItem> orderItemList; // 订单项目列表（每个项含merchantId，必传）
    // 移除merchantId字段：不再全局传，仅在每个OrderItem中传
}