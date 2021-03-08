package com.quguai.campustransaction.order.interceptor;


import com.quguai.common.constant.AuthServerConstant;
import com.quguai.common.vo.MemberResponseVo;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberResponseVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        MemberResponseVo attribute = (MemberResponseVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute == null) {
            request.getSession().setAttribute("msg", "请先进行登录");
            String url = request.getRequestURI();
            response.sendRedirect("http://auth.campus.com/login.html?returnUrl=" + url);
        } else {
            loginUser.set(attribute);
        }
        return attribute != null;
    }
}
