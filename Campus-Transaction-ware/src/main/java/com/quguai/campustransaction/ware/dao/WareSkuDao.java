package com.quguai.campustransaction.ware.dao;

import com.quguai.campustransaction.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品库存
 * 
 * @author liyang
 * @email liyang@gmail.com
 * @date 2021-01-20 21:50:27
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {
	
}
