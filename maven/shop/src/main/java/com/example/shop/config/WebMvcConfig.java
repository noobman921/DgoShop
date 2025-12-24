package com.example.shop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // 本地图片存储的绝对路径（根据自己的系统修改）
    private static final String UPLOAD_PATH = "file:D:/shop_uploads/";
    // 项目内存储（开发用）的路径（可选）
    // private static final String UPLOAD_PATH = "classpath:/static/uploads/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射规则：前端访问 /uploads/** 时，后端指向 UPLOAD_PATH 目录
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(UPLOAD_PATH);
    }
}