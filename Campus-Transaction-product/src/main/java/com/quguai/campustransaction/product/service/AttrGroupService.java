package com.quguai.campustransaction.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quguai.campustransaction.product.entity.AttrEntity;
import com.quguai.campustransaction.product.vo.AttrGroupRelationVO;
import com.quguai.campustransaction.product.vo.AttrGroupWithAttrsVo;
import com.quguai.common.utils.PageUtils;
import com.quguai.campustransaction.product.entity.AttrGroupEntity;

import java.util.List;
import java.util.Map;

/**
 * ???Է??
 *
 * @author liyang
 * @email liyang@gmail.com
 * @date 2021-01-20 20:57:30
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long categoryId);

    void deleteRelation(List<AttrGroupRelationVO> vos);

    List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId);
}

