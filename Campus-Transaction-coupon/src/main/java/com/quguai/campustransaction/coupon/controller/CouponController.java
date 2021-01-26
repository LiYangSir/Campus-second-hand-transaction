package com.quguai.campustransaction.coupon.controller;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import com.quguai.campustransaction.coupon.entity.CouponEntity;
import com.quguai.campustransaction.coupon.service.CouponService;
import com.quguai.common.utils.PageUtils;
import com.quguai.common.utils.R;

@RefreshScope
@RestController
@RequestMapping("coupon/coupon")
public class CouponController {
    @Autowired
    private CouponService couponService;

    @Value("${coupon.user.age}")
    private Integer age;
    @Value("${coupon.user.name}")
    private String name;

    // 测试nacos-config
    @GetMapping("config")
    public R getConfig() {
        return R.ok().put("age", age).put("name", name);
    }

    // 测试openfeign
    @GetMapping("/arraylist")
    public R getCoupon() {
        CouponEntity couponEntity = new CouponEntity();
        couponEntity.setCouponName("Hello Coupon");
        return R.ok().put("arrays", Arrays.asList(couponEntity, couponEntity));
    }


    @RequestMapping("/list")
    //@RequiresPermissions("coupon:coupon:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = couponService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("coupon:coupon:info")
    public R info(@PathVariable("id") Long id){
		CouponEntity coupon = couponService.getById(id);

        return R.ok().put("coupon", coupon);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("coupon:coupon:save")
    public R save(@RequestBody CouponEntity coupon){
		couponService.save(coupon);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("coupon:coupon:update")
    public R update(@RequestBody CouponEntity coupon){
		couponService.updateById(coupon);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("coupon:coupon:delete")
    public R delete(@RequestBody Long[] ids){
		couponService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
