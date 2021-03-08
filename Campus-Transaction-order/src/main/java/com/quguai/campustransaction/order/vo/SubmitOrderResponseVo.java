package com.quguai.campustransaction.order.vo;

import com.quguai.campustransaction.order.entity.OrderEntity;
import com.quguai.campustransaction.order.entity.OrderItemEntity;
import com.quguai.campustransaction.order.to.OrderCreateTo;
import lombok.Data;

@Data
public class SubmitOrderResponseVo {
    private Integer code;
    private OrderEntity order;
}
