package com.quguai.campustransaction.product.service.impl;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quguai.common.utils.PageUtils;
import com.quguai.common.utils.Query;

import com.quguai.campustransaction.product.dao.SkuInfoDao;
import com.quguai.campustransaction.product.entity.SkuInfoEntity;
import com.quguai.campustransaction.product.service.SkuInfoService;
import org.springframework.util.StringUtils;
import sun.nio.cs.ext.MacArabic;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.save(skuInfoEntity);
    }

    /**
     * 会携带 key, catelogId, brandId, min, max
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (StringUtils.hasText(key)) {
            queryWrapper.and(wrapper -> wrapper.eq("sku_id", key).or().like("sku_name", key));
        }
        String catelogId = (String) params.get("catelogId");
        if (StringUtils.hasText(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            queryWrapper.and(wrapper -> wrapper.eq("catalog_id", catelogId));
        }
        String brandId = (String) params.get("brandId");
        if (StringUtils.hasText(brandId) && !"0".equalsIgnoreCase(brandId)) {
            queryWrapper.and(wrapper -> wrapper.eq("brand_id", brandId));
        }
        String min = (String) params.get("min");
        if (StringUtils.hasText(min)) {
            queryWrapper.ge("price", min);
        }
        String max = (String) params.get("max");
        if (StringUtils.hasText(max) && !max.equals("0")) {
            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                if (bigDecimal.compareTo(BigDecimal.ZERO) > 0){
                    queryWrapper.and(wrapper -> wrapper.le("price", max));
                }
            } catch (Exception e) {

            }

        }


        IPage<SkuInfoEntity> page = this.page(new Query<SkuInfoEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        List<SkuInfoEntity> list = this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
        return list;
    }

}