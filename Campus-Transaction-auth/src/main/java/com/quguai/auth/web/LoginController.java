package com.quguai.auth.web;

import com.alibaba.fastjson.TypeReference;
import com.quguai.auth.feign.MemberFeignService;
import com.quguai.auth.feign.ThirdPartyFeignService;
import com.quguai.auth.vo.UserLoginVo;
import com.quguai.auth.vo.UserRegister;
import com.quguai.common.constant.AuthServerConstant;
import com.quguai.common.exception.BizCodeEnum;
import com.quguai.common.utils.R;
import com.quguai.common.vo.MemberResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class LoginController {

    @Autowired
    private ThirdPartyFeignService thirdPartyFeignService;

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("/login.html")
    public String login(@RequestParam(value = "returnUrl", required = false) String url, HttpSession session) {
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute != null) {
            return "redirect:http://campus.com";
        }
        return "login";
    }

    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone) {
        //TODO 接口防刷

        // 解决了60s内重复刷新的问题，使用redis缓存解决
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String redisCode = valueOperations.get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (StringUtils.hasText(redisCode)) {
            long oldTime = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - oldTime < 60000) {
                return R.error(BizCodeEnum.VALID_SMS_CODE);
            }
        }

        String code = UUID.randomUUID().toString().substring(0, 5);
        valueOperations.set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, code + "_" + System.currentTimeMillis(), 30, TimeUnit.MINUTES);
        thirdPartyFeignService.sendCode(phone, code);
        return R.ok();
    }

    /**
     * TODO 重定向携带数据，利用session的原理，将数据放置到session中。只要跳到下一个界面，session的数据就会删除
     * TODO 分布式下的session问题
     *
     * @param userRegister
     * @param attributes
     * @param result
     * @return
     */

    @PostMapping("/registered")
    public String register(@Valid UserRegister userRegister, BindingResult result, RedirectAttributes attributes) {
        if (result.hasErrors()) {
            Map<String, String> collect = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            attributes.addFlashAttribute("errors", collect);
            return "redirect:http://auth.campus.com/register.html";
        }
        Map<String, String> error = new HashMap<>();
        // 1. 验证验证码的正确性
        String code = userRegister.getCode();
        String s = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + userRegister.getPhone());

        if (StringUtils.hasText(s) && code.equals(s.split("_")[0])) {
            // 调用远程服务完成注册功能
            R response = memberFeignService.register(userRegister);
            if (response.getCode() == BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode()) {
                error.put("phone", response.getMsg());
            }
            if (response.getCode() == BizCodeEnum.USERNAME_EXIST_EXCEPTION.getCode()) {
                error.put("username", response.getMsg());
            }
            // 删除验证码
            redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + userRegister.getPhone());
        } else {
            error.put("code", "验证码错误");
        }
        // 如果存在错误
        if (error.size() != 0) {
            attributes.addFlashAttribute("errors", error);
            return "redirect:http://auth.campus.com/register.html";
        }
        // 调用远程服务完成注册
        return "redirect:http://auth.campus.com/login.html";
    }

    @PostMapping("/login")
    public String login(UserLoginVo userLoginVo, RedirectAttributes attributes, HttpSession session) {
        R login = memberFeignService.login(userLoginVo);
        if (login.getCode() != 0) {
            String msg = login.getMsg();
            attributes.addFlashAttribute("error", msg);
            return "redirect:http://auth.campus.com/login.html";
        }
        MemberResponseVo data = login.getData(new TypeReference<MemberResponseVo>() {
        });
        session.setAttribute(AuthServerConstant.LOGIN_USER, data);
        return "redirect:http://campus.com";
    }
}
