package com.quguai.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.quguai.auth.config.OAuthWeiboProperties;
import com.quguai.auth.feign.MemberFeignService;
import com.quguai.auth.service.OAuthWeiboService;
import com.quguai.auth.util.HttpUtils;
import com.quguai.common.vo.MemberResponseVo;
import com.quguai.auth.vo.SocialUser;
import com.quguai.common.utils.R;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Service
public class OAuthWeiboServiceImpl implements OAuthWeiboService {

    @Autowired
    private OAuthWeiboProperties weiboProperties;

    @Autowired
    private MemberFeignService memberFeignService;
    @Override
    public boolean getTokenByCode(String code, HttpSession session) throws Exception {
        // 根据code获取token
        // https://api.weibo.com/oauth2/access_token?client_id=YOUR_CLIENT_ID&client_secret=YOUR_CLIENT_SECRET&grant_type=authorization_code&redirect_uri=YOUR_REGISTERED_REDIRECT_URI&code=CODE
        Map<String, String> request = new HashMap<>();
        request.put("client_id", weiboProperties.getClientId());
        request.put("client_secret", weiboProperties.getClientSecret());
        request.put("grant_type", "authorization_code");
        request.put("redirect_uri", weiboProperties.getRedirectUri());
        request.put("code", code);
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", new HashMap<>(), request, "");
        //SocialUser postForObject = restTemplate.postForObject("https://api.weibo.com/oauth2/access_token", request, SocialUser.class);
        if (response.getStatusLine().getStatusCode() == 200) {
            SocialUser socialUser = JSON.parseObject(EntityUtils.toString(response.getEntity()), SocialUser.class);
            // 获取了token,如果第一次进来，那么就自动注册进来
            R r = memberFeignService.oauthLogin(socialUser);
            if (r.getCode() == 0) {
                MemberResponseVo responseVo = r.getData(new TypeReference<MemberResponseVo>() {});
                session.setAttribute("loginUser", responseVo);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
