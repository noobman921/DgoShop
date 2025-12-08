package com.example.shop.entity;

import lombok.Data;

@Data
public class User {
    private Long userId;
    private String username;
    private String account;
    private String password;
}
