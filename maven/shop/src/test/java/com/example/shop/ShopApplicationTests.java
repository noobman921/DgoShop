package com.example.shop;

import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@MapperScan("com.example.shop.mapper")
class ShopApplicationTests {

	@Test
	void contextLoads() {
	}

}
