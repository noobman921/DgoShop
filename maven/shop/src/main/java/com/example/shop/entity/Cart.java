package com.example.shop.entity;

import lombok.Data;

@Data
public class Cart {
    private Long userId;
    private Long productId;
    private Integer quantity;
}
