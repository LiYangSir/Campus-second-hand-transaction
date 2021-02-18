package com.quguai.campustransaction.search.vo;

import com.quguai.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.List;

@Data
public class SearchResult {

    private List<SkuEsModel> products;

    private Long total;
    private Integer pageNum;
    private Integer totalPages;

    private List<BrandVo> brands;
    private List<CatalogVo> catalogs;
    private List<AttrVo> attrs;


    @Data
    public static class BrandVo{
        private Integer brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class CatalogVo{
        private Integer catalogId;
        private String catalogName;
    }

    @Data
    public static class AttrVo{
        private Integer attrId;
        private String attrName;
        private List<String> attrValue ;
    }
}
