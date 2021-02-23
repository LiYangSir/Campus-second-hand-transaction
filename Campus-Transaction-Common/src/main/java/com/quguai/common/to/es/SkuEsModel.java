package com.quguai.common.to.es;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuEsModel {

    private Long skuId;
    private Long spuId;
    private Long brandId;
    private Long catalogId;
    private String skuTitle;
    private Long saleCount;

    private BigDecimal skuPrice;
    private String skuImg;
    private Boolean hasStock;
    private Long hotScore;
    private String brandName;
    private String brandImg;
    private String catalogName;

    private List<Attrs> attrs;

    @Data
    public static class Attrs{
        private Long attrId;
        private String attrName;
        private String attrValue;
    }
}
