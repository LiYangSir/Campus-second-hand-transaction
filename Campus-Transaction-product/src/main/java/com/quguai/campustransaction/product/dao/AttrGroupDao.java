package com.quguai.campustransaction.product.dao;

import com.quguai.campustransaction.product.entity.AttrAttrgroupRelationEntity;
import com.quguai.campustransaction.product.entity.AttrGroupEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ???Է??
 * 
 * @author liyang
 * @email liyang@gmail.com
 * @date 2021-01-20 20:57:30
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    void deleteBatchRelation(@Param("entities") List<AttrAttrgroupRelationEntity> entities);
}
