package com.quguai.campustransaction.order.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.thymeleaf.util.ListUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class OrderConfirmVo {

    @Getter @Setter
    private List<MemberAddressVo> address;
    @Getter @Setter
    private List<OrderItemVo> orderItemVos;

    // 发票信息。。。

    // 优惠卷信息
    @Getter @Setter
    private Integer integration;
    @Getter @Setter
    Map<Long, Boolean> stocks;

    @Getter @Setter // 防重令牌
    private String orderToken;

    public Integer getCount(){
        Integer count = 0;
        if (!ListUtils.isEmpty(orderItemVos)) {
            for (OrderItemVo item : orderItemVos) {
                count += item.getCount();
            }
        }
        return count;
    }
    public BigDecimal getTotal() {
        BigDecimal sumPrice = new BigDecimal(0);
        if (!ListUtils.isEmpty(orderItemVos)) {
            for (OrderItemVo item : orderItemVos) {
                BigDecimal multiply = item.getPrice().multiply(BigDecimal.valueOf(item.getCount()));
                sumPrice = sumPrice.add(multiply);
            }
        }
        return sumPrice;
    }

    public BigDecimal getPayPrice() {
        return getTotal();
    }
}
