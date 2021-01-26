package com.quguai.campustransaction.member.feign;

import com.quguai.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

@Service
@FeignClient(value = "Campus-Transaction-coupon")
public interface CouponFeignService {
    @GetMapping("coupon/coupon/arraylist")
    R getCoupon();
}
