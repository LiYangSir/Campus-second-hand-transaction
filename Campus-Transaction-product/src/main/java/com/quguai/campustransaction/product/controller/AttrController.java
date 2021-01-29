package com.quguai.campustransaction.product.controller;

import com.quguai.campustransaction.product.entity.AttrEntity;
import com.quguai.campustransaction.product.entity.ProductAttrValueEntity;
import com.quguai.campustransaction.product.service.AttrService;
import com.quguai.campustransaction.product.service.ProductAttrValueService;
import com.quguai.campustransaction.product.vo.AttrResponseVo;
import com.quguai.campustransaction.product.vo.AttrVo;
import com.quguai.common.utils.PageUtils;
import com.quguai.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * ??Ʒ?
 *
 * @author liyang
 * @email liyang@gmail.com
 * @date 2021-01-20 21:19:10
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {

    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;


    @GetMapping("/base/listforspu/{spuId}")
    public R baseAttrList(@PathVariable("spuId") Long spuId) {
        List<ProductAttrValueEntity> entityList = productAttrValueService.getBaseAttrList(spuId);
        return R.ok().put("data", entityList);
    }


    @GetMapping("/{attrType}/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String, Object> params, @PathVariable("attrType")String attrType, @PathVariable("catelogId") Long catelogId) {
        PageUtils page = attrService.queryBaseAttrPage(params, catelogId, attrType);
        return R.ok().put("page", page);
    }

    @RequestMapping("/list")
    //@RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }

    @RequestMapping("/info/{attrId}")
    //@RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId) {
        AttrResponseVo attrResponseVo = attrService.getAttrInfo(attrId);

        return R.ok().put("attr", attrResponseVo);
    }

    @RequestMapping("/save")
    //@RequiresPermissions("product:attr:save")
    public R save(@RequestBody AttrVo attr) {
        attrService.saveAttr(attr);

        return R.ok();
    }


    @RequestMapping("/update")
    //@RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttrVo attr) {
        attrService.updateAttr(attr);

        return R.ok();
    }

    @PostMapping("/update/{spuId}")
    //@RequiresPermissions("product:attr:update")
    public R updateSpuAttr(@PathVariable("spuId") Long spuId, @RequestBody List<ProductAttrValueEntity> valueEntities) {
        productAttrValueService.updateSpuAttr(spuId, valueEntities);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds) {
        attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
