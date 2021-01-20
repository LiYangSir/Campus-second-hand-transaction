package com.quguai.campustransaction.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quguai.common.utils.PageUtils;
import com.quguai.campustransaction.ware.entity.WareInfoEntity;

import java.util.Map;

/**
 * 仓库信息
 *
 * @author liyang
 * @email liyang@gmail.com
 * @date 2021-01-20 21:50:27
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

