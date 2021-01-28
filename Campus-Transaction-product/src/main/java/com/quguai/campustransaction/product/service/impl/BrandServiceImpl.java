package com.quguai.campustransaction.product.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quguai.campustransaction.product.dao.CategoryBrandRelationDao;
import com.quguai.campustransaction.product.entity.CategoryBrandRelationEntity;
import com.quguai.campustransaction.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quguai.common.utils.PageUtils;
import com.quguai.common.utils.Query;

import com.quguai.campustransaction.product.dao.BrandDao;
import com.quguai.campustransaction.product.entity.BrandEntity;
import com.quguai.campustransaction.product.service.BrandService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String) params.get("key");
        QueryWrapper<BrandEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(key)) {
            queryWrapper.eq("brand_id", key).or().like("name", key);
        }
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void updateDetail(BrandEntity brand) {
        this.updateById(brand);
        // 修改中间表
        if (StringUtils.hasLength(brand.getName())) {
            categoryBrandRelationService.updateBrand(brand.getBrandId(), brand.getName());
            // TODO 更新其他关联
        }
    }

}