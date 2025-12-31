package com.example.shop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 关键修改：添加线上前端域名（解决403核心）
        config.addAllowedOrigin("http://8.134.119.70:8081");
        // 保留本地开发的域名（兼容本地调试）
        config.addAllowedOrigin("http://localhost:3000");

        // 允许跨域请求携带Cookie（和前端withCredentials=true匹配，必须保留）
        config.setAllowCredentials(true);
        // 允许所有请求方法（GET/POST/PUT/DELETE等）
        config.addAllowedMethod("*");
        // 允许所有请求头（Content-Type/Token等）
        config.addAllowedHeader("*");
        // 新增：预检请求缓存时间（优化性能，避免频繁OPTIONS请求）
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 所有接口都允许跨域
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}