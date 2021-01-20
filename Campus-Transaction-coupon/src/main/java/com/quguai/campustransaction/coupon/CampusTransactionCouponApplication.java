package com.quguai.campustransaction.coupon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.quguai.campustransaction.coupon.dao")
@SpringBootApplication
public class CampusTransactionCouponApplication {

	public static void main(String[] args) {
		SpringApplication.run(CampusTransactionCouponApplication.class, args);
	}

}
