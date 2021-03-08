package com.quguai.campustransaction.order.to;

import com.quguai.campustransaction.order.entity.OrderEntity;
import com.quguai.campustransaction.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateTo {
    private OrderEntity order;
    private List<OrderItemEntity> orderItems;
    private BigDecimal payPrice;
    private BigDecimal fare;
}
