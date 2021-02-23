package com.quguai.campustransaction.product.vo;

import com.quguai.campustransaction.product.entity.SkuImagesEntity;
import com.quguai.campustransaction.product.entity.SkuInfoEntity;
import com.quguai.campustransaction.product.entity.SpuInfoDescEntity;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class SkuItemVo {

    private SkuInfoEntity skuInfo;
    private List<SkuImagesEntity> images;

    private List<SkuItemSaleAttrVo> saleAttr;
    private SpuInfoDescEntity descEntity;
    private List<SpuItemAttrGroupAttr> groupAttrs;




}
