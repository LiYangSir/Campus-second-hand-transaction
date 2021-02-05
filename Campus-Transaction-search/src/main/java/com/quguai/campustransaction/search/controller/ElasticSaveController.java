package com.quguai.campustransaction.search.controller;

import com.quguai.campustransaction.search.service.ProductSaveService;
import com.quguai.common.exception.BizCodeEnum;
import com.quguai.common.to.es.SkuEsModel;
import com.quguai.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/search/save")
public class ElasticSaveController {

    @Autowired
    private ProductSaveService productSaveService;

    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels) {
        boolean b = false;  // 没有错误
        try {
            b = productSaveService.productStatusUp(skuEsModels);
        } catch (IOException e) {
            log.error("ElasticSearch上架出现错误: {}", e.getMessage());
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION);
        }
        if (!b){
            return R.ok();
        }else {
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION);
        }
    }
}
