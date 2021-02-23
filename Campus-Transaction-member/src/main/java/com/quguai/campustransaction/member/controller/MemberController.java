package com.quguai.campustransaction.member.controller;

import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.quguai.campustransaction.member.exception.PhoneNumberExistException;
import com.quguai.campustransaction.member.exception.UsernameExistException;
import com.quguai.campustransaction.member.feign.CouponFeignService;
import com.quguai.campustransaction.member.vo.SocialUser;
import com.quguai.campustransaction.member.vo.UserLoginVo;
import com.quguai.campustransaction.member.vo.UserRegister;
import com.quguai.common.exception.BizCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.quguai.campustransaction.member.entity.MemberEntity;
import com.quguai.campustransaction.member.service.MemberService;
import com.quguai.common.utils.PageUtils;
import com.quguai.common.utils.R;



/**
 * ??Ա
 *
 * @author liyang
 * @email liyang@gmail.com
 * @date 2021-01-20 21:47:04
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private CouponFeignService couponFeignService;

    @PostMapping("/register")
    public R register(@RequestBody UserRegister vo){
        try {
            memberService.register(vo);
        } catch (PhoneNumberExistException phoneNumberExistException) {
            // 根据不同的异常做出不同的处理
            return R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION);
        } catch (UsernameExistException usernameExistException) {
            return R.error(BizCodeEnum.USERNAME_EXIST_EXCEPTION);
        }
        return R.ok();
    }

    @PostMapping("/oauth2/login")
    public R oauthLogin(@RequestBody SocialUser socialUser) throws Exception {
        MemberEntity entity = memberService.login(socialUser);
        if (entity != null) {
            return R.ok().setData(entity);
        }
        return R.error(BizCodeEnum.LOGIN_ACCOUNT_PASSWORD_ERROR_EXCEPTION);
    }

    @PostMapping("/login")
    public R login(@RequestBody UserLoginVo userLoginVo) {
        MemberEntity entity = memberService.login(userLoginVo);
        if (entity != null) {
            return R.ok().setData(entity);
        }
        return R.error(BizCodeEnum.LOGIN_ACCOUNT_PASSWORD_ERROR_EXCEPTION);
    }

    @GetMapping("arraylist")
    public R getCoupon() {
        return R.ok().put("member", "member").put("coupon", couponFeignService.getCoupon().get("arrays"));
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
