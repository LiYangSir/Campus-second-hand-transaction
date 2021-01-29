package com.quguai.campustransaction.ware.feign;

import com.quguai.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("Campus-Transaction-product")
public interface ProductFeignService {

    @RequestMapping("product/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);
    }
