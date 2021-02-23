package com.quguai.auth.web;

import com.quguai.auth.feign.MemberFeignService;
import com.quguai.auth.service.OAuthWeiboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

// /oauth2.0/weibo/sucess?code=90376f2b6f5aa0763a0fbd65ebffad2c
@Controller
@RequestMapping("/oauth2.0")
public class OAuth2Controller {

    @Autowired
    private OAuthWeiboService authWeiboService;

    @GetMapping("/weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession session) throws Exception {

        boolean loginSuccess = authWeiboService.getTokenByCode(code, session);
        return loginSuccess ? "redirect:http://campus.com" : "redirect:http://auth.campus.com/login.html";
    }
}
