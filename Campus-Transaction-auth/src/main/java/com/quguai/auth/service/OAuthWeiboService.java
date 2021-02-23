package com.quguai.auth.service;

import javax.servlet.http.HttpSession;

public interface OAuthWeiboService {
    boolean getTokenByCode(String code, HttpSession session) throws Exception;
}
