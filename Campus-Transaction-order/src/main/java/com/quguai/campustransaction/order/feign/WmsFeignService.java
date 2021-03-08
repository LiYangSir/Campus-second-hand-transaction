package com.quguai.campustransaction.order.feign;

import com.quguai.campustransaction.order.vo.WareSkuLockVo;
import com.quguai.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("Campus-Transaction-ware")
public interface WmsFeignService {

    @PostMapping("/ware/waresku/hasstock")
    R getSkuHasStock(@RequestBody List<Long> skuIds);

    @GetMapping("ware/waresku/fare")
    R getFare(@RequestParam("arrId") Long addrId);

    @PostMapping("ware/waresku/lock/order")
    R orderLockStock(@RequestBody WareSkuLockVo vo);
}
