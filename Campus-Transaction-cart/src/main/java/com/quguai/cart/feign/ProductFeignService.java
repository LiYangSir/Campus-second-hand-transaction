package com.quguai.cart.feign;

import com.quguai.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.List;

@FeignClient("Campus-Transaction-product")
public interface ProductFeignService {

    @RequestMapping("product/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);

    @ResponseBody
    @GetMapping("product/skusaleattrvalue/stringlist/{skuId}")
    List<String> getSkuSaleAttr(@PathVariable("skuId") Long skuId);

    @ResponseBody
    @GetMapping("/product/skuinfo/{skuId}/price")
    BigDecimal getPrice(@PathVariable("skuId") Long skuId);
}
