package com.example.shop.vo;

import java.math.BigDecimal;

import lombok.Data;

/**
 * 订单项VO（合并商品信息，取消嵌套）
 */
@Data
public class OrderItemVO {
    // 订单项原有字段
    private Long id; // 订单项ID
    private String orderNo; // 关联订单号
    private Integer quantity; // 购买数量
    private Long merchantId; // 商家ID
    private Long productId; // 商品ID（保留，便于关联）

    // 商品信息字段（直接嵌入，取消ProductVO嵌套）
    private String productName; // 商品名称
    private BigDecimal productPrice;// 商品单价
}