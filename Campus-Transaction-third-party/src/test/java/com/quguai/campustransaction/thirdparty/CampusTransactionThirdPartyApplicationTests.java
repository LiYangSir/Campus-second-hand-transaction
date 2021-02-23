package com.quguai.campustransaction.thirdparty;

import com.aliyun.oss.OSSClient;
import com.quguai.campustransaction.thirdparty.component.SmsComponent;
import com.quguai.campustransaction.thirdparty.util.HttpUtils;
import org.apache.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class CampusTransactionThirdPartyApplicationTests {

    @Resource
    private OSSClient ossClient;

    @Autowired
    private SmsComponent smsComponent;

    @Test
    void contextLoads() throws FileNotFoundException {

        InputStream inputStream = new FileInputStream("C:/Users/LiYangSir/Pictures/redis.png");
        ossClient.putObject("campus-transaction", "redis.png", inputStream);

        // 关闭OSSClient。
        ossClient.shutdown();
    }

    @Test
    void sendSms() {
        String host = "https://gyytz.market.alicloudapi.com";
        String path = "/sms/smsSend";
        String method = "POST";
        String appcode = "b791e6168f6f4d83aad2ead106c89c75";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", "17612733884");
        querys.put("param", "**code**:1212,**minute**:12");
        querys.put("smsSignId", "2e65b1bb3d054466b82f0c9d125465e2");
        querys.put("templateId", "908e94ccf08b4476ba6c876d13f084ad");
        Map<String, String> bodys = new HashMap<String, String>();

        try {
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSms2(){
        smsComponent.sendSmdCode("17612733884", "5211314");
    }

}
