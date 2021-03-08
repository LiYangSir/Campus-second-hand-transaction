package com.quguai.campustransaction.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quguai.campustransaction.product.dao.SpuInfoDao;
import com.quguai.campustransaction.product.entity.*;
import com.quguai.campustransaction.product.feign.CouponFeignService;
import com.quguai.campustransaction.product.feign.SearchFeignService;
import com.quguai.campustransaction.product.feign.WareFeignService;
import com.quguai.campustransaction.product.service.*;
import com.quguai.campustransaction.product.vo.SpuSaveVo;
import com.quguai.campustransaction.product.vo.spu.*;
import com.quguai.common.constant.ProductConstant;
import com.quguai.common.to.SkuHasStockTo;
import com.quguai.common.to.SkuReductionTo;
import com.quguai.common.to.SpuBoundsTo;
import com.quguai.common.to.es.SkuEsModel;
import com.quguai.common.utils.PageUtils;
import com.quguai.common.utils.Query;
import com.quguai.common.utils.R;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private SpuImagesService spuImagesService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

//    @GlobalTransactional
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo spuSaveVo) {
        // 1. 保存SPU的基本信息
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuSaveVo, spuInfoEntity);
        this.saveBaseSpuInfo(spuInfoEntity);
        Long spuId = spuInfoEntity.getId();
        // 2. 保存SPU的描述图片
        List<String> description = spuSaveVo.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(spuInfoEntity.getId());
        descEntity.setDecript(String.join(",", description));
        spuInfoDescService.saveSpuInfoDesc(descEntity);

        // 3. 保存spu的图片集
        List<String> images = spuSaveVo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(), images);

        // 4. 保存spu的规格参数
        List<BaseAttrs> baseAttrs = spuSaveVo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setAttrId(attr.getAttrId());
            valueEntity.setAttrName(attrService.getById(attr.getAttrId()).getAttrName());
            valueEntity.setAttrValue(attr.getAttrValues());
            valueEntity.setQuickShow(attr.getShowDesc());
            valueEntity.setSpuId(spuInfoEntity.getId());
            return valueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveProductAttr(collect);

        // 5.spu的积分信息
        Bounds bounds = spuSaveVo.getBounds();
        SpuBoundsTo spuBoundsTo = new SpuBoundsTo();
        BeanUtils.copyProperties(bounds, spuBoundsTo);
        spuBoundsTo.setSpuId(spuInfoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundsTo);
        if (r.getCode() != 0) {
            log.error("远程服务保存spu积分信息失败");
        }

        // 6. 保存当前spu对应的所有sku信息

        List<Skus> skus = spuSaveVo.getSkus();
        if (skus != null && skus.size() > 0) {
            skus.forEach(sku -> {
                String defaultImage = "";
                for (Images image : sku.getImages()) {
                    if (image.getDefaultImg() == 1)
                        defaultImage = image.getImgUrl();
                }
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku, skuInfoEntity);
                skuInfoEntity.setBrandId(spuSaveVo.getBrandId());
                skuInfoEntity.setCatalogId(spuSaveVo.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuId);
                skuInfoEntity.setSkuDefaultImg(defaultImage);
                // 6.1 sku的基本信息，sku_info
                skuInfoService.saveSkuInfo(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();

                List<SkuImagesEntity> collectImage = sku.getImages().stream().filter(image -> StringUtils.hasText(image.getImgUrl()))
                        .map(image -> {
                            SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                            skuImagesEntity.setSkuId(skuId);
                            skuImagesEntity.setImgUrl(image.getImgUrl());
                            skuImagesEntity.setDefaultImg(image.getDefaultImg());
                            return skuImagesEntity;
                        }).collect(Collectors.toList());
                // 6.2 sku的pms_sku_images
                skuImagesService.saveBatch(collectImage);


                List<Attr> attr = sku.getAttr();
                List<SkuSaleAttrValueEntity> saleAttrValueEntities = attr.stream().map(attr1 -> {
                    SkuSaleAttrValueEntity valueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr1, valueEntity);
                    valueEntity.setSkuId(skuId);
                    return valueEntity;
                }).collect(Collectors.toList());
                // 6.3 sku销售属性信息
                skuSaleAttrValueService.saveBatch(saleAttrValueEntities);
                // 6.4 sku的优惠 满减信息
                SkuReductionTo skuReduction = new SkuReductionTo();
                BeanUtils.copyProperties(sku, skuReduction);
                skuReduction.setSkuId(skuId);
                if (skuReduction.getFullCount() > 0 || skuReduction.getFullPrice().compareTo(BigDecimal.ZERO) > 0) {
                    R r1 = couponFeignService.saveSkuReduction(skuReduction);
                    if (r1.getCode() != 0) {
                        log.error("远程服务保存sku满减优惠信息信息失败");
                    }
                }
            });
        }
    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.save(spuInfoEntity);
    }

    /**
     * 有可能携带 status, brandId, catelog_id, key
     *
     * @param params
     * @return
     */
    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (StringUtils.hasText(key)) {
            queryWrapper.and(wrapper -> {
                wrapper.eq("id", key).or().like("spu_name", key);
            });
        }
        String status = (String) params.get("status");
        if (StringUtils.hasText(status)) {
            queryWrapper.and(wrapper -> {
                wrapper.eq("publish_status", status);
            });
        }
        String catelogId = (String) params.get("catelogId");
        if (StringUtils.hasText(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            queryWrapper.and(wrapper -> {
                wrapper.eq("catalog_id", catelogId);
            });
        }
        String brandId = (String) params.get("brandId");
        if (StringUtils.hasText(brandId) && !"0".equalsIgnoreCase(brandId)) {
            queryWrapper.and(wrapper -> {
                wrapper.eq("brand_id", brandId);
            });
        }
        IPage<SpuInfoEntity> page = this.page(new Query<SpuInfoEntity>().getPage(params), queryWrapper);

        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);
        List<Long> skuIds = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        // 3. 查出所有可以被检索的的规格属性
        List<ProductAttrValueEntity> baseAttrList = productAttrValueService.getBaseAttrList(spuId);
        List<Long> attrIds = baseAttrList.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());

        List<Long> collect1 = attrService.selectSearchAttrs(attrIds);
        Set<Long> idSet = new HashSet<>(collect1);

        List<SkuEsModel.Attrs> attrsList = baseAttrList.stream().filter(entity ->
                idSet.contains(entity.getAttrId())).map(entity -> {
            SkuEsModel.Attrs attrs1 = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(entity, attrs1);
            return attrs1;
        }).collect(Collectors.toList());

        // 1.发送远程调用查看是否有库存
        Map<Long, Boolean> skuHasStockMap = null;
        try {
            R r = wareFeignService.getSkuHasStock(skuIds);
            TypeReference<List<SkuHasStockTo>> typeReference = new TypeReference<List<SkuHasStockTo>>() {};
            skuHasStockMap = r.getData(typeReference).stream().collect(Collectors.toMap(SkuHasStockTo::getSkuId, SkuHasStockTo::getHasStock));
        } catch (Exception e){
            log.error("库存服务调用异常，原因：{}", e);
        }

        // 分装每一个sku的信息
        Map<Long, Boolean> finalSkuHasStockMap = skuHasStockMap;
        List<SkuEsModel> upProducts = skus.stream().map(sku -> {
            // 组装需要的数据
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, skuEsModel);
            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());
            // 设置是否有库存
            if (finalSkuHasStockMap == null){
                skuEsModel.setHasStock(true);
            }else{
                skuEsModel.setHasStock(finalSkuHasStockMap.get(sku.getSkuId()));
            }
            // 2. 热度评分：0
            skuEsModel.setHotScore(0L);
            BrandEntity brandEntity = brandService.getById(sku.getBrandId());
            skuEsModel.setBrandName(brandEntity.getName());
            skuEsModel.setBrandImg(brandEntity.getLogo());
            CategoryEntity categoryEntity = categoryService.getById(sku.getCatalogId());
            skuEsModel.setCatalogName(categoryEntity.getName());
            // 设置检索属性
            skuEsModel.setAttrs(attrsList);
            return skuEsModel;
        }).collect(Collectors.toList());
        // 4. 将数据发送给ES进行保存，远程调用ES服务
        R r = searchFeignService.productStatusUp(upProducts);
        if (r.getCode() == 0) {
            // 远程调用成功
            //TODO 修改发布状态，已经上架
            baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        } else {
            // TODO 重复调用？接口幂等性，重试机制
        }
    }

    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {
        SkuInfoEntity byId = skuInfoService.getById(skuId);
        return getById(byId.getSpuId());
    }
}