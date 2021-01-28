package com.quguai.campustransaction.product.controller;

import com.quguai.campustransaction.product.entity.AttrEntity;
import com.quguai.campustransaction.product.entity.AttrGroupEntity;
import com.quguai.campustransaction.product.service.AttrAttrgroupRelationService;
import com.quguai.campustransaction.product.service.AttrGroupService;
import com.quguai.campustransaction.product.service.AttrService;
import com.quguai.campustransaction.product.service.CategoryService;
import com.quguai.campustransaction.product.vo.AttrGroupRelationVO;
import com.quguai.common.utils.PageUtils;
import com.quguai.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * ???Է??
 *
 * @author liyang
 * @email liyang@gmail.com
 * @date 2021-01-20 21:19:10
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService relationService;

    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelationVO> vos) {
        relationService.saveBatch(vos);
        return R.ok();
    }

    @GetMapping("{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId) {
        List<AttrEntity> entities = attrService.getRelationAttr(attrgroupId);
        return R.ok().put("data", entities);
    }

    @GetMapping("{attrgroupId}/noattr/relation")
    public R attrNoRelation(@RequestParam Map<String, Object> params, @PathVariable("attrgroupId") Long attrgroupId) {
        PageUtils page = attrService.getNoRelationAttr(params, attrgroupId);
        return R.ok().put("data", page);
    }

    @RequestMapping("/list/{catelogId}")
    //@RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params, @PathVariable("catelogId") Long catelogId) {
//        PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params, catelogId);
        return R.ok().put("page", page);
    }


    @RequestMapping("/info/{attrGroupId}")
    //@RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

        List<Long> catelogPath = categoryService.findCatelogPath(attrGroup.getCatelogId());
        attrGroup.setCatelogPath(catelogPath);

        return R.ok().put("attrGroup", attrGroup);
    }

    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return R.ok();
    }

    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

    @PostMapping("/attr/relation/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody List<AttrGroupRelationVO> vos) {
        attrGroupService.deleteRelation(vos);

        return R.ok();
    }

}
