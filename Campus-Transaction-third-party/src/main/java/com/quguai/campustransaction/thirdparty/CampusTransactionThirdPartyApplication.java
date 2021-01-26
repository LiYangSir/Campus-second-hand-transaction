package com.quguai.campustransaction.thirdparty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class CampusTransactionThirdPartyApplication {

	public static void main(String[] args) {
		SpringApplication.run(CampusTransactionThirdPartyApplication.class, args);
	}

}
