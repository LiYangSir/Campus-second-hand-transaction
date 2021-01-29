package com.quguai.campustransaction.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quguai.campustransaction.ware.vo.MergeVo;
import com.quguai.campustransaction.ware.vo.PurchaseDoneVo;
import com.quguai.common.utils.PageUtils;
import com.quguai.campustransaction.ware.entity.PurchaseEntity;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author liyang
 * @email liyang@gmail.com
 * @date 2021-01-20 21:50:27
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);


    PageUtils queryPageUnReceive(Map<String, Object> params);

    void mergePurchase(MergeVo mergeVo);

    void received(List<Long> ids);

    void done(PurchaseDoneVo vo);
}

