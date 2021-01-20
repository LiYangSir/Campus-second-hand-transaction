package com.quguai.campustransaction.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quguai.common.utils.PageUtils;
import com.quguai.campustransaction.order.entity.OrderEntity;

import java.util.Map;

/**
 * ????
 *
 * @author liyang
 * @email liyang@gmail.com
 * @date 2021-01-20 21:49:03
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

