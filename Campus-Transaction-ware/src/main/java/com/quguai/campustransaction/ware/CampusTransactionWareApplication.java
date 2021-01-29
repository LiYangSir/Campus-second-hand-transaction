package com.quguai.campustransaction.ware;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableFeignClients("com.quguai.campustransaction.ware.feign")
@EnableTransactionManagement
@EnableDiscoveryClient
@SpringBootApplication
public class CampusTransactionWareApplication {

	public static void main(String[] args) {
		SpringApplication.run(CampusTransactionWareApplication.class, args);
	}

}
