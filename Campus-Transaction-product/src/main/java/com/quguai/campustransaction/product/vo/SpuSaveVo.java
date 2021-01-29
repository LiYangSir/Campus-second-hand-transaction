/**
  * Copyright 2021 bejson.com 
  */
package com.quguai.campustransaction.product.vo;
import com.quguai.campustransaction.product.vo.spu.BaseAttrs;
import com.quguai.campustransaction.product.vo.spu.Bounds;
import com.quguai.campustransaction.product.vo.spu.Skus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SpuSaveVo {

    private String spuName;
    private String spuDescription;
    private Long catalogId;
    private Long brandId;
    private BigDecimal weight;
    private int publishStatus;
    private List<String> decript;
    private List<String> images;
    private Bounds bounds;
    private List<BaseAttrs> baseAttrs;
    private List<Skus> skus;

}