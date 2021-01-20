package com.quguai.campustransaction.member;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.quguai.campustransaction.member.dao")
@SpringBootApplication
public class CampusTransactionMemberApplication {

	public static void main(String[] args) {
		SpringApplication.run(CampusTransactionMemberApplication.class, args);
	}

}
