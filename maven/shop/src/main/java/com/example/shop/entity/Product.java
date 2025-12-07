package com.example.shop.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class Product {
    private Long productId;
    private String productName;
    private String productDesc;
    private Integer stock;
    private String productPic;
    private BigDecimal productPrice;
    private Long merchantId;
}
