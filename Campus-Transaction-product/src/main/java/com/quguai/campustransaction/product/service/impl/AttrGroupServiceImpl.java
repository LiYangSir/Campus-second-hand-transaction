package com.quguai.campustransaction.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quguai.campustransaction.product.dao.AttrGroupDao;
import com.quguai.campustransaction.product.entity.AttrAttrgroupRelationEntity;
import com.quguai.campustransaction.product.entity.AttrGroupEntity;
import com.quguai.campustransaction.product.service.AttrAttrgroupRelationService;
import com.quguai.campustransaction.product.service.AttrGroupService;
import com.quguai.campustransaction.product.service.AttrService;
import com.quguai.campustransaction.product.vo.AttrGroupRelationVO;
import com.quguai.campustransaction.product.vo.AttrGroupWithAttrsVo;
import com.quguai.campustransaction.product.vo.SkuItemVo;
import com.quguai.campustransaction.product.vo.SpuItemAttrGroupAttr;
import com.quguai.common.utils.PageUtils;
import com.quguai.common.utils.Query;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {


    @Autowired
    private AttrAttrgroupRelationService relationService;

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<AttrGroupEntity>();

        if (Strings.isNotBlank(key)) {
            queryWrapper.and(obj -> {
                obj.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }
        if (catelogId == 0) {
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), queryWrapper);
            return new PageUtils(page);
        } else {
            queryWrapper.eq("catelog_id", catelogId);
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), queryWrapper);
            return new PageUtils(page);
        }
    }

    @Transactional
    @Override
    public void deleteRelation(List<AttrGroupRelationVO> vos) {
        List<AttrAttrgroupRelationEntity> entities = vos.stream().map(item -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());
        attrGroupDao.deleteBatchRelation(entities);
    }

    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        return attrGroupEntities.stream().map(attrGroupEntity -> {
            AttrGroupWithAttrsVo attrGroupWithAttrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(attrGroupEntity, attrGroupWithAttrsVo);
            attrGroupWithAttrsVo.setAttrs(attrService.getRelationAttr(attrGroupEntity.getAttrGroupId()));
            return attrGroupWithAttrsVo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<SpuItemAttrGroupAttr> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {
        return this.baseMapper.getAttrGroupWithAttrsBySpuId(spuId, catalogId);
    }
}