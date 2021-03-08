package com.quguai.cart.vo;

import org.thymeleaf.util.ListUtils;

import java.math.BigDecimal;
import java.util.List;

public class Cart {
    private List<CartItem> items;
    private Integer countNum;
    private Integer countType;
    private BigDecimal totalAmount;
    private BigDecimal reduce = new BigDecimal("0.00");

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int count = 0;
        if (!ListUtils.isEmpty(items)) {
            for (CartItem item : items) {
                count += item.getCount();
            }
        }
        return count;
    }

    public Integer getCountType() {
        return ListUtils.isEmpty(items)? 0: items.size();
    }

    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal(0);
        if (!ListUtils.isEmpty(items)) {
            for (CartItem item : items) {
                if (item.getCheck())
                    amount = amount.add(item.getTotalPrice());
            }
        }
        return amount.subtract(getReduce());
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
