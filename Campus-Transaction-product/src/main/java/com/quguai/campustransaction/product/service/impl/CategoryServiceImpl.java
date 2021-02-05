package com.quguai.campustransaction.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quguai.campustransaction.product.dao.CategoryDao;
import com.quguai.campustransaction.product.entity.CategoryEntity;
import com.quguai.campustransaction.product.service.CategoryBrandRelationService;
import com.quguai.campustransaction.product.service.CategoryService;
import com.quguai.campustransaction.product.vo.Catelog2Vo;
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
        while (entity.getParentCid() != 0) {
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

    @Override
    public List<CategoryEntity> getLevelFirstCategories() {
        return this.list(new QueryWrapper<CategoryEntity>().eq("cat_level", 1));
    }

    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        List<CategoryEntity> levelFirstCategories = this.getLevelFirstCategories();
        return levelFirstCategories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<CategoryEntity> level2 = this.list(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
            if (level2 != null) {
                return level2.stream().map(entity -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), entity.getCatId().toString(), entity.getName(), null);
                    List<CategoryEntity> level3 = this.list(new QueryWrapper<CategoryEntity>().eq("parent_cid", entity.getCatId()));
                    if (level3 != null) {
                        List<Catelog2Vo.Catelog3Vo> collect = level3.stream().map(item -> new Catelog2Vo.Catelog3Vo(entity.getCatId().toString(), item.getCatId().toString(), item.getName())).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return null;
        }));
    }

    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJSON() {
        List<CategoryEntity> list = this.list();
        return list.stream().filter(entity -> entity.getParentCid() == 0).collect(Collectors.toMap(v -> v.getCatId().toString(),
                v -> list.stream()
                        .filter(entity -> entity.getParentCid().equals(v.getCatId()))
                        .map(entity -> {
                            Catelog2Vo catelog2Vo = new Catelog2Vo(entity.getParentCid().toString(), entity.getCatId().toString(), entity.getName(), null);
                            List<Catelog2Vo.Catelog3Vo> collect = list.stream()
                                    .filter(entity1 -> entity1.getParentCid().equals(entity.getCatId()))
                                    .map(entity1 -> new Catelog2Vo.Catelog3Vo(entity1.getParentCid().toString(), entity1.getCatId().toString(), entity1.getName()))
                                    .collect(Collectors.toList());
                            catelog2Vo.setCatalog3List(collect);
                            return catelog2Vo;
                        }).collect(Collectors.toList())));
    }
}