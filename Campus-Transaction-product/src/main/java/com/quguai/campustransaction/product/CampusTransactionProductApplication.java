package com.quguai.campustransaction.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 启用devtools
 * 1. 关闭thymeleaf缓存
 * 2. 导入依赖
 * 3. ctrl+f9 或者 ctrl+shift+f9重新编译
 */
@EnableDiscoveryClient
@EnableFeignClients("com.quguai.campustransaction.product.feign")
@MapperScan("com.quguai.campustransaction.product.dao")
@SpringBootApplication
public class CampusTransactionProductApplication {

	public static void main(String[] args) {
		SpringApplication.run(CampusTransactionProductApplication.class, args);
	}

}
