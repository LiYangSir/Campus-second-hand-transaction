package com.quguai.campustransaction.ware.service.impl;

import com.quguai.campustransaction.ware.entity.PurchaseDetailEntity;
import com.quguai.campustransaction.ware.service.PurchaseDetailService;
import com.quguai.campustransaction.ware.service.WareSkuService;
import com.quguai.campustransaction.ware.vo.MergeVo;
import com.quguai.campustransaction.ware.vo.PurchaseDoneItemVo;
import com.quguai.campustransaction.ware.vo.PurchaseDoneVo;
import com.quguai.common.constant.WareConstant;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quguai.common.utils.PageUtils;
import com.quguai.common.utils.Query;

import com.quguai.campustransaction.ware.dao.PurchaseDao;
import com.quguai.campustransaction.ware.entity.PurchaseEntity;
import com.quguai.campustransaction.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;

    @Autowired
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnReceive(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().in("status", Arrays.asList(0, 1))
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {
        Long purchaseId = mergeVo.getPurchaseId();
        if (purchaseId == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATE_STATUS.getCode());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }
        // TODO 确定采购单状态是 0， 1 才可以
        List<Long> items = mergeVo.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = items.stream().map(item -> {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            detailEntity.setId(item);
            detailEntity.setPurchaseId(finalPurchaseId);
            detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return detailEntity;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(collect);
    }

    @Override
    public void received(List<Long> ids) {
        // 1. 确认当前采购单是新建或者已分配状态
        List<PurchaseEntity> collect = ids.stream()
                .map(this::getById)
                .filter(entity ->
                        entity.getStatus() == WareConstant.PurchaseStatusEnum.CREATE_STATUS.getCode() ||
                                entity.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getCode())
                .map(entity -> {entity.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
                    return entity;})
                .collect(Collectors.toList());

        // 2. 改变采购单状态
        this.updateBatchById(collect);

        // 3. 改变采购项的状态
        collect.forEach(entity -> {
            List<PurchaseDetailEntity> entities = purchaseDetailService.listDetailByPurchaseId(entity.getId());
            List<PurchaseDetailEntity> collect1 = entities.stream().map(purchaseDetailEntity -> {
                PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
                detailEntity.setId(purchaseDetailEntity.getId());
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
                return detailEntity;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(collect1);
        });
    }

    @Transactional
    @Override
    public void done(PurchaseDoneVo vo) {

        // 改变采购项目状态
        Boolean flag = true;
        List<PurchaseDoneItemVo> items = vo.getItems();
        List<PurchaseDetailEntity> updates = new ArrayList<>();
        for (PurchaseDoneItemVo item : items) {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HAS_ERROR.getCode()) {
                flag = false;
                detailEntity.setStatus(item.getStatus());
            }else{
                PurchaseDetailEntity entity = purchaseDetailService.getById(item.getItemId());
                // 将成功的采购入库
                wareSkuService.addStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum());
                detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());
            }
            detailEntity.setId(item.getItemId());
            updates.add(detailEntity);
        }
        purchaseDetailService.updateBatchById(updates);

        // 改变采购单状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(vo.getId());
        purchaseEntity.setStatus(flag? WareConstant.PurchaseStatusEnum.FINISH.getCode() : WareConstant.PurchaseStatusEnum.HAS_ERROR.getCode());
        this.updateById(purchaseEntity);


    }

}