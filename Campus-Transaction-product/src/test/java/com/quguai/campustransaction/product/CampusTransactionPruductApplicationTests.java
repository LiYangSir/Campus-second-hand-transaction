package com.quguai.campustransaction.product;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quguai.campustransaction.product.entity.BrandEntity;
import com.quguai.campustransaction.product.service.AttrGroupService;
import com.quguai.campustransaction.product.service.BrandService;
import com.quguai.campustransaction.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@Slf4j
@SpringBootTest
class CampusTransactionPruductApplicationTests {

    @Autowired
    private BrandService service;

    @Autowired
    private RedissonClient redissonClient;

    @Resource
    private BaseMapper<BrandEntity> baseMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

//    @Autowired
//    private OSSClient ossClient;

    @Autowired
    private CategoryService categoryService;

    @Test
    public void testUpload() throws FileNotFoundException {
////        // Endpoint以杭州为例，其它Region请按实际情况填写。
////        String endpoint = "http://oss-cn-beijing.aliyuncs.com";
////		// 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建。
////        String accessKeyId = "LTAI4FzB4pFzaBczoLcM7EZ9";
////        String accessKeySecret = "jzv28lCKyszIKRPMWMxfrUwCkDfwnC";
////
////		// 创建OSSClient实例。
////        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
//
//		// 上传文件流。
//        InputStream inputStream = new FileInputStream("C:/Users/LiYangSir/Pictures/redis.png");
//        ossClient.putObject("campus-transaction", "redis.png", inputStream);
//
//		// 关闭OSSClient。
//        ossClient.shutdown();
    }

    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setDescript("");
        brandEntity.setName("huawei");
        service.save(brandEntity);
        log.info("保存成功");
    }

    @Test
    void test() {
//       categoryService.findCatelogPath(225L).forEach(System.out::println);
        Page<BrandEntity> pp = baseMapper.selectPage(new Page<>(1, 10), null);
        System.out.println();
    }

    @Test
    void testRedis() {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        System.out.println(valueOperations.get("name"));
    }

    @Test
    void JsonTest(){
        System.out.println(JSON.toJSONString(null));
    }

    @Test
    void testRedissonClient(){
        System.out.println(redissonClient);
    }
}
