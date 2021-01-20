package com.quguai.campustransaction.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quguai.common.utils.PageUtils;
import com.quguai.campustransaction.order.entity.PaymentInfoEntity;

import java.util.Map;

/**
 * ֧????Ϣ?
 *
 * @author liyang
 * @email liyang@gmail.com
 * @date 2021-01-20 21:49:02
 */
public interface PaymentInfoService extends IService<PaymentInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

