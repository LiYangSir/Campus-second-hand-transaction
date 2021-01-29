package com.quguai.campustransaction.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableDiscoveryClient
@EnableFeignClients("com.quguai.campustransaction.product.feign")
@MapperScan("com.quguai.campustransaction.product.dao")
@SpringBootApplication
public class CampusTransactionProductApplication {

	public static void main(String[] args) {
		SpringApplication.run(CampusTransactionProductApplication.class, args);
	}

}
