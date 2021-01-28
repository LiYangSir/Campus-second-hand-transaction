package com.quguai.campustransaction.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quguai.campustransaction.product.entity.BrandEntity;
import com.quguai.common.utils.PageUtils;
import com.quguai.campustransaction.product.entity.CategoryBrandRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * Ʒ?Ʒ???????
 *
 * @author liyang
 * @email liyang@gmail.com
 * @date 2021-01-20 20:57:30
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveDetail(CategoryBrandRelationEntity categoryBrandRelation);

    void updateBrand(Long brandId, String name);

    void updateCategory(Long catId, String name);

    List<BrandEntity> getBrandsByCatId(Long catId);
}

