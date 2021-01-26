package com.quguai.campustransaction.thirdparty;

import com.aliyun.oss.OSSClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@SpringBootTest
class CampusTransactionThirdPartyApplicationTests {

	@Resource
	private OSSClient ossClient;
	@Test
	void contextLoads() throws FileNotFoundException {

        InputStream inputStream = new FileInputStream("C:/Users/LiYangSir/Pictures/redis.png");
        ossClient.putObject("campus-transaction", "redis.png", inputStream);

		// 关闭OSSClient。
        ossClient.shutdown();
	}

}
