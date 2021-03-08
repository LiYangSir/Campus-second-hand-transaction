package com.quguai.campustransaction.order.vo;

import lombok.Data;
import org.thymeleaf.util.ListUtils;

import java.util.List;

@Data
public class WareSkuLockVo {
    private String orderSn;
    private List<OrderItemVo> lock;
}
