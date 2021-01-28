package com.quguai.campustransaction.product.dao;

import com.quguai.campustransaction.product.entity.CategoryBrandRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Ʒ?Ʒ???????
 * 
 * @author liyang
 * @email liyang@gmail.com
 * @date 2021-01-20 20:57:30
 */
@Mapper
public interface CategoryBrandRelationDao extends BaseMapper<CategoryBrandRelationEntity> {

    void updateCategory(@Param("catId") Long catId, @Param("name") String name);
}
