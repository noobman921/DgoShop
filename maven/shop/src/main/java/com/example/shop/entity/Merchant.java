package com.example.shop.entity;

import lombok.Data;

@Data
public class Merchant {
    private Long merchantId;
    private String merchantName;
    private String merchantAccount;
    private String merchantPassword;
}
