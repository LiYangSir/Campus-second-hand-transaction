package com.quguai.campustransaction.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quguai.campustransaction.product.entity.SpuInfoDescEntity;
import com.quguai.campustransaction.product.vo.SpuSaveVo;
import com.quguai.common.utils.PageUtils;
import com.quguai.campustransaction.product.entity.SpuInfoEntity;

import java.util.Map;

/**
 * spu??Ϣ
 *
 * @author liyang
 * @email liyang@gmail.com
 * @date 2021-01-20 20:57:29
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuSaveVo spuSaveVo);

    void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity);

    PageUtils queryPageByCondition(Map<String, Object> params);
}

