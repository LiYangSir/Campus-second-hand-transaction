package com.quguai.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "campus.oauth.weibo")
public class OAuthWeiboProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
}
