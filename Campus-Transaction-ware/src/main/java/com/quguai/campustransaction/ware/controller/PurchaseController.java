package com.quguai.campustransaction.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.quguai.campustransaction.ware.vo.MergeVo;
import com.quguai.campustransaction.ware.vo.PurchaseDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.quguai.campustransaction.ware.entity.PurchaseEntity;
import com.quguai.campustransaction.ware.service.PurchaseService;
import com.quguai.common.utils.PageUtils;
import com.quguai.common.utils.R;



/**
 * 采购信息
 *
 * @author liyang
 * @email liyang@gmail.com
 * @date 2021-01-20 21:50:27
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;


    @PostMapping("/done")
    public R finish(@RequestBody PurchaseDoneVo vo) {

        purchaseService.done(vo);
        return R.ok();
    }

    @PostMapping("/receive")
    public R received(@RequestBody List<Long> ids) {

        purchaseService.received(ids);
        return R.ok();
    }

    @PostMapping("/merge")
    //@RequiresPermissions("ware:purchase:list")
    public R merge(@RequestBody MergeVo mergeVo) {

        purchaseService.mergePurchase(mergeVo);

        return R.ok();
    }

    @RequestMapping("/unreceive/list")
    //@RequiresPermissions("ware:purchase:list")
    public R unReceiveList(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPageUnReceive(params);

        return R.ok().put("page", page);
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:purchase:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:purchase:info")
    public R info(@PathVariable("id") Long id){
		PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:purchase:save")
    public R save(@RequestBody PurchaseEntity purchase){
		purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:purchase:update")
    public R update(@RequestBody PurchaseEntity purchase){
		purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:purchase:delete")
    public R delete(@RequestBody Long[] ids){
		purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
