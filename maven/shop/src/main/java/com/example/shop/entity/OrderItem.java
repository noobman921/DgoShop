package com.example.shop.entity;

import lombok.Data;

@Data
public class OrderItem {
    private Long Id;
    private String orderNo;
    private Long productId;
    private Integer quantity;
    private Long merchantId;
}
