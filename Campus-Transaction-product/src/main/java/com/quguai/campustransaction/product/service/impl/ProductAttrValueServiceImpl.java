package com.quguai.campustransaction.product.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quguai.common.utils.PageUtils;
import com.quguai.common.utils.Query;

import com.quguai.campustransaction.product.dao.ProductAttrValueDao;
import com.quguai.campustransaction.product.entity.ProductAttrValueEntity;
import com.quguai.campustransaction.product.service.ProductAttrValueService;
import org.springframework.transaction.annotation.Transactional;


@Service("productAttrValueService")
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity> implements ProductAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductAttrValueEntity> page = this.page(
                new Query<ProductAttrValueEntity>().getPage(params),
                new QueryWrapper<ProductAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveProductAttr(List<ProductAttrValueEntity> collect) {
        this.saveBatch(collect);
    }

    @Override
    public List<ProductAttrValueEntity> getBaseAttrList(Long spuId) {
        List<ProductAttrValueEntity> valueEntities = this.list(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));
        return valueEntities;
    }

    @Transactional
    @Override
    public void updateSpuAttr(Long spuId, List<ProductAttrValueEntity> valueEntities) {
        // 删除之前的数据
        this.remove(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));

        List<ProductAttrValueEntity> collect = valueEntities.stream().map(entity -> {
            entity.setSpuId(spuId);
            return entity;
        }).collect(Collectors.toList());
        this.saveBatch(collect);
    }

}