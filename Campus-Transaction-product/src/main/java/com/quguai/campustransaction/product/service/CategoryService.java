package com.quguai.campustransaction.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quguai.campustransaction.product.vo.Catelog2Vo;
import com.quguai.common.utils.PageUtils;
import com.quguai.campustransaction.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * ??Ʒ???????
 *
 * @author liyang
 * @email liyang@gmail.com
 * @date 2021-01-20 20:57:30
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenuByIds(List<Long> asList);

    List<Long> findCatelogPath(Long catelogId);

    void updateCascade(CategoryEntity category);

    List<CategoryEntity> getLevelFirstCategories();

    Map<String, List<Catelog2Vo>> getCatelogJsonFromCache();

    Map<String, List<Catelog2Vo>> getCatelogJSONFromDbWithLocalLock();
}

