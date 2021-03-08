package com.quguai.campustransaction.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quguai.campustransaction.product.vo.SkuItemSaleAttrVo;
import com.quguai.common.utils.PageUtils;
import com.quguai.campustransaction.product.entity.SkuSaleAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * sku????????&ֵ
 *
 * @author liyang
 * @email liyang@gmail.com
 * @date 2021-01-20 20:57:29
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SkuItemSaleAttrVo> getSaleAttrBySpuId(Long spuId);

    List<String> getSkuSaleAttrAsStringList(Long skuId);
}

