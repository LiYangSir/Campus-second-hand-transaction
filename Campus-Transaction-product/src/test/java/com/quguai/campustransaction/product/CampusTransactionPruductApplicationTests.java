package com.quguai.campustransaction.product;

import com.quguai.campustransaction.product.entity.BrandEntity;
import com.quguai.campustransaction.product.service.BrandService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class CampusTransactionPruductApplicationTests {

	@Autowired
	private BrandService service;

	@Test
	void contextLoads() {
		BrandEntity brandEntity = new BrandEntity();
		brandEntity.setDescript("");
		brandEntity.setName("huawei");
		service.save(brandEntity);
		log.info("保存成功");
	}

}
