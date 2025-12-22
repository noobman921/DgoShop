package com.example.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // 商品ID
    private Long productId;
    // 商品名称
    private String productName;
    // 购买数量
    private Integer quantity;
    // 商品单价
    private BigDecimal productPrice;
}