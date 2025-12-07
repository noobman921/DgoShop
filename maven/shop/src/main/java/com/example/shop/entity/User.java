package com.example.shop.entity;

import lombok.Data;

@Data
public class User {
    private Long userId;
    private String userName;
    private String account;
    private String password;
}
