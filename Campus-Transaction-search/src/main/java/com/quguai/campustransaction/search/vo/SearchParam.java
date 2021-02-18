package com.quguai.campustransaction.search.vo;

import lombok.Data;

import java.util.List;

@Data
public class SearchParam {
    private String keyword;
    private Long catalog3Id;
    /**
     * sort=saleCount_asc
     * sort=skuPrice_asc
     * sort=hotScore_asc_desc
     */
    private String sort;
    private Integer hasStock = 1;
    private String skuPrice;
    private List<Long> brandId;
    private List<String> attrs;
    private Integer pageNumber = 1;

}
