package com.quguai.campustransaction.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quguai.campustransaction.product.dao.BrandDao;
import com.quguai.campustransaction.product.dao.CategoryBrandRelationDao;
import com.quguai.campustransaction.product.dao.CategoryDao;
import com.quguai.campustransaction.product.entity.BrandEntity;
import com.quguai.campustransaction.product.entity.CategoryBrandRelationEntity;
import com.quguai.campustransaction.product.entity.CategoryEntity;
import com.quguai.campustransaction.product.service.BrandService;
import com.quguai.campustransaction.product.service.CategoryBrandRelationService;
import com.quguai.common.utils.PageUtils;
import com.quguai.common.utils.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Autowired
    private BrandDao brandDao;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private BrandService brandService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();
        BrandEntity brandEntity = brandDao.selectById(brandId);
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);

        categoryBrandRelation.setBrandName(brandEntity.getName());
        categoryBrandRelation.setCatelogName(categoryEntity.getName());

        this.save(categoryBrandRelation);
    }

    @Override
    public void updateBrand(Long brandId, String name) {
        CategoryBrandRelationEntity entity = new CategoryBrandRelationEntity();
        entity.setBrandId(brandId);
        entity.setBrandName(name);
        this.update(entity, new UpdateWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId));
    }

    @Override
    public void updateCategory(Long catId, String name) {
        this.baseMapper.updateCategory(catId, name);
    }

    @Override
    public List<BrandEntity> getBrandsByCatId(Long catId) {
        List<CategoryBrandRelationEntity> brandRelationEntities = this.list(new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));
        List<Long> collect = brandRelationEntities.stream().map(CategoryBrandRelationEntity::getBrandId).collect(Collectors.toList());
        return brandService.listByIds(collect);
    }

}