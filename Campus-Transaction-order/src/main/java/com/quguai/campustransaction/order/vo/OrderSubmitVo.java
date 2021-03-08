package com.quguai.campustransaction.order.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderSubmitVo {
    private Long addrId;
    private Integer payType;
    // 无需获取提交的商品
    private String orderToken;
    private BigDecimal payPrice; // 应付价格，验价
    private String note;
}
