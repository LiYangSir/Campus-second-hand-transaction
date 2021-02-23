package com.quguai.campustransaction.product.service.impl;

import com.quguai.campustransaction.product.entity.SkuImagesEntity;
import com.quguai.campustransaction.product.entity.SpuInfoDescEntity;
import com.quguai.campustransaction.product.entity.SpuInfoEntity;
import com.quguai.campustransaction.product.service.*;
import com.quguai.campustransaction.product.vo.SkuItemSaleAttrVo;
import com.quguai.campustransaction.product.vo.SkuItemVo;
import com.quguai.campustransaction.product.vo.SpuItemAttrGroupAttr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quguai.common.utils.PageUtils;
import com.quguai.common.utils.Query;

import com.quguai.campustransaction.product.dao.SkuInfoDao;
import com.quguai.campustransaction.product.entity.SkuInfoEntity;
import org.springframework.util.StringUtils;
import sun.nio.cs.ext.MacArabic;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private SkuSaleAttrValueService attrValueService;

    @Autowired
    private ThreadPoolExecutor executor;

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

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();

        // 1. 基本信息的获取
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity infoEntity = this.getById(skuId);
            skuItemVo.setSkuInfo(infoEntity);
            return infoEntity;
        }, executor);

        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync(skuInfoEntity -> {
            // 3. 获取spu的销售属性组合
            List<SkuItemSaleAttrVo> skuItemSaleAttrVos = attrValueService.getSaleAttrBySpuId(skuInfoEntity.getSpuId());
            skuItemVo.setSaleAttr(skuItemSaleAttrVos);
        }, executor);

        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync(skuInfoEntity -> {
            // 4. 获取spu的介绍
            SpuInfoDescEntity infoDesc = spuInfoDescService.getById(skuInfoEntity.getSpuId());
            skuItemVo.setDescEntity(infoDesc);
        }, executor);

        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            // 2. sku的图片的信息
            List<SkuImagesEntity> skuImages = skuImagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(skuImages);
        }, executor);

        CompletableFuture<Void> groupFuture = infoFuture.thenAcceptAsync(skuInfoEntity -> {
            // 5. 获取spu的规格参数
            List<SpuItemAttrGroupAttr> attrGroup = attrGroupService.getAttrGroupWithAttrsBySpuId(skuInfoEntity.getSpuId(), skuInfoEntity.getCatalogId());
            skuItemVo.setGroupAttrs(attrGroup);
        }, executor);

        CompletableFuture.allOf(imageFuture, saleAttrFuture, descFuture, groupFuture).get();
        return skuItemVo;
    }

}