package com.quguai.campustransaction.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quguai.campustransaction.product.dao.CategoryDao;
import com.quguai.campustransaction.product.entity.CategoryEntity;
import com.quguai.campustransaction.product.service.CategoryBrandRelationService;
import com.quguai.campustransaction.product.service.CategoryService;
import com.quguai.common.utils.PageUtils;
import com.quguai.common.utils.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {


    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public void removeMenuByIds(List<Long> asList) {
        // TODO 检查当前删除的菜单是否被别的地方所引用
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public List<Long> findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        CategoryEntity entity = this.getById(catelogId);
        paths.add(entity.getCatId());
        while (entity.getParentCid() != 0){
            paths.add(entity.getParentCid());
            entity = this.getById(entity.getParentCid());
        }
        Collections.reverse(paths);
        return paths;
    }

    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        if (StringUtils.hasLength(category.getName())) {
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
            // TODO 更新其他表中数据
        }
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        List<CategoryEntity> entities = baseMapper.selectList(null);
        return entities.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                .map(categoryEntity -> {
                    categoryEntity.setChildren(getChildren(categoryEntity, entities));
                    return categoryEntity;
                })
                .sorted(Comparator.comparingInt(o -> (o.getSort() == null ? 0 : o.getSort())))
                .collect(Collectors.toList());
    }

    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        return all.stream().filter(entity -> entity.getParentCid().equals(root.getCatId()))
                .map(entity -> {
                    entity.setChildren(getChildren(entity, all));
                    return entity;
                })
                .sorted(Comparator.comparingInt(o -> (o.getSort() == null ? 0 : o.getSort())))
                .collect(Collectors.toList());
    }

}