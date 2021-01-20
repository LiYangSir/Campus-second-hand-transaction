package com.quguai.campustransaction.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quguai.common.utils.PageUtils;
import com.quguai.campustransaction.product.entity.ProductAttrValueEntity;

import java.util.Map;

/**
 * spu????ֵ
 *
 * @author liyang
 * @email liyang@gmail.com
 * @date 2021-01-20 20:57:30
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

