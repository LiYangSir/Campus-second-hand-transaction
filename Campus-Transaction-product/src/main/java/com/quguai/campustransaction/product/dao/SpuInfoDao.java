package com.quguai.campustransaction.product.dao;

import com.quguai.campustransaction.product.entity.SpuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * spu??Ϣ
 * 
 * @author liyang
 * @email liyang@gmail.com
 * @date 2021-01-20 20:57:29
 */
@Mapper
public interface SpuInfoDao extends BaseMapper<SpuInfoEntity> {

    void updateSpuStatus(@Param("spuId") Long spuId, @Param("code") int code);
}
