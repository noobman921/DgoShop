package com.example.shop.mapper;

import com.example.shop.entity.Product;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Mapper
@Repository
public interface ProductMapper {
    @Select("SELECT * FROM product WHERE product_id = #{productId}")
    Product selectByProductId(Long productId);
}
