package com.quguai.campustransaction.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.quguai.campustransaction.search.config.ElasticSearchConfig;
import com.quguai.campustransaction.search.constant.EsConstant;
import com.quguai.campustransaction.search.service.MallSearchService;
import com.quguai.campustransaction.search.vo.SearchParam;
import com.quguai.campustransaction.search.vo.SearchResult;
import com.quguai.common.to.es.SkuEsModel;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.join.ScoreMode;
import org.checkerframework.checker.units.qual.A;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.util.ListUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Resource
    private RestHighLevelClient client;

    @Override
    public SearchResult search(SearchParam searchParam) throws IOException {
        SearchRequest request = buildSearchRequest(searchParam);

        SearchResponse response = client.search(request, ElasticSearchConfig.COMMON_OPTIONS);
        return buildSearchResult(searchParam, response);
    }

    private SearchResult buildSearchResult(SearchParam searchParam, SearchResponse response) {
        SearchResult searchResult = new SearchResult();
        SearchHits hits = response.getHits();
        List<SkuEsModel> skuEsModelList = new ArrayList<>();
        if (hits.getHits() != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                if (StringUtils.hasText(searchParam.getKeyword())) {
                    Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                    skuEsModel.setSkuTitle(highlightFields.get("skuTitle").getFragments()[0].string());
                }
                skuEsModelList.add(skuEsModel);
            }
        }
        searchResult.setProducts(skuEsModelList);
        // 分类信息
        Aggregations aggregations = response.getAggregations();
        ParsedLongTerms catalog_agg = aggregations.get("catalog_agg");
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        for (Terms.Bucket bucket : catalog_agg.getBuckets()) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            catalogVo.setCatalogId(bucket.getKeyAsNumber().intValue());
            ParsedStringTerms catalogNameAgg = bucket.getAggregations().get("catalog_name_agg");
            String value = catalogNameAgg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(value);
            catalogVos.add(catalogVo);
        }
        searchResult.setCatalogs(catalogVos);

        // 品牌信息
        ParsedLongTerms brandAgg = aggregations.get("brand_agg");
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        for (Terms.Bucket bucket : brandAgg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            brandVo.setBrandId(bucket.getKeyAsNumber().intValue());
            ParsedStringTerms brandNameAgg = bucket.getAggregations().get("brand_name_agg");
            String brandName = brandNameAgg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brandName);
            ParsedStringTerms brandImgAgg = bucket.getAggregations().get("brand_img_agg");
            String brandImg = brandImgAgg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(brandImg);
            brandVos.add(brandVo);
        }
        searchResult.setBrands(brandVos);

        // 属性信息
        ParsedNested attr_agg = aggregations.get("attr_agg");
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedLongTerms attrIdAgg = attr_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            attrVo.setAttrId(bucket.getKeyAsNumber().intValue());
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attr_name_agg");
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attr_value_agg");
            attrVo.setAttrName(attrName);
            List<String> attrValue = attrValueAgg.getBuckets().stream().map(MultiBucketsAggregation.Bucket::getKeyAsString).collect(Collectors.toList());
            attrVo.setAttrValue(attrValue);

            attrVos.add(attrVo);
        }
        searchResult.setAttrs(attrVos);

        searchResult.setPageNum(searchParam.getPageNumber());
        long total = hits.getTotalHits().value;
        searchResult.setTotal(total);
        long totalPage = total % EsConstant.PRODUCT_PAGE_SIZE == 0 ? total / EsConstant.PRODUCT_PAGE_SIZE : (total / EsConstant.PRODUCT_PAGE_SIZE + 1);
        searchResult.setTotalPages((int) totalPage);

        return searchResult;
    }

    private SearchRequest buildSearchRequest(SearchParam searchParam) {
        SearchSourceBuilder builder = new SearchSourceBuilder();

        // 模糊匹配
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        if (StringUtils.hasText(searchParam.getKeyword())) {
            queryBuilder.must(QueryBuilders.matchQuery("skuTitle", searchParam.getKeyword()));
        }
        // 过滤操作：不增加得分
        if (searchParam.getCatalog3Id() != null) {
            queryBuilder.filter(QueryBuilders.termQuery("catalogId", searchParam.getCatalog3Id()));
        }
        if (searchParam.getBrandId() != null && searchParam.getBrandId().size() > 0) {
            queryBuilder.filter(QueryBuilders.termsQuery("brandId", searchParam.getBrandId()));
        }

        // 按照属性进行查询
        if (!ListUtils.isEmpty(searchParam.getAttrs())) {
            for (String attrStr : searchParam.getAttrs()) {
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                String[] s = attrStr.split("_");
                String attrId = s[0];
                String[] attrValue = s[1].split(":");
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValue));
                NestedQueryBuilder attrs = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                queryBuilder.filter(attrs);
            }
        }
        if (searchParam.getHasStock() != null) {
            // 按照库存进行查询
            queryBuilder.filter(QueryBuilders.termsQuery("hasStock", searchParam.getHasStock() == 1));
        }

        // 按照价格进行查询
        String skuPrice = searchParam.getSkuPrice();
        if (StringUtils.hasText(skuPrice)) {
            RangeQueryBuilder price = QueryBuilders.rangeQuery("skuPrice");

            String[] s = skuPrice.split("_");
            if (StringUtils.hasText(s[0])) {
                price.gte(s[0]);
            }
            if (StringUtils.hasText(s[1])) {
                price.lte(s[1]);
            }
            queryBuilder.filter(price);
        }

        // 进行bool完全拼装
        builder.query(queryBuilder);

        // 排序
        if (StringUtils.hasText(searchParam.getSort())) {
            String sort = searchParam.getSort();
            String[] s = sort.split("_");

            builder.sort(s[0], SortOrder.fromString(s[1]));
        }
        // 分页
        builder.from((searchParam.getPageNumber() - 1) * EsConstant.PRODUCT_PAGE_SIZE);
        builder.size(EsConstant.PRODUCT_PAGE_SIZE);

        // 高亮
        if (StringUtils.hasText(searchParam.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            builder.highlighter(highlightBuilder);
        }

        // 聚合
        TermsAggregationBuilder brandIdAgg = AggregationBuilders.terms("brand_agg").field("brandId").size(50);
        TermsAggregationBuilder brandNameAgg = AggregationBuilders.terms("brand_name_agg").field("brandName").size(1);
        TermsAggregationBuilder brandImgAgg = AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1);
        brandIdAgg.subAggregation(brandNameAgg);
        brandIdAgg.subAggregation(brandImgAgg);
        builder.aggregation(brandIdAgg);

        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        TermsAggregationBuilder catalogNameAgg = AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1);
        catalogAgg.subAggregation(catalogNameAgg);
        builder.aggregation(catalogAgg);

        NestedAggregationBuilder nested = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        TermsAggregationBuilder attrNameAgg = AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1);
        TermsAggregationBuilder attrValueAgg = AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50);
        attrIdAgg.subAggregation(attrNameAgg);
        attrIdAgg.subAggregation(attrValueAgg);
        nested.subAggregation(attrIdAgg);
        builder.aggregation(nested);

        System.out.println(builder.toString());
        return new SearchRequest(new String[]{"campus_product"}, builder);
    }

}
