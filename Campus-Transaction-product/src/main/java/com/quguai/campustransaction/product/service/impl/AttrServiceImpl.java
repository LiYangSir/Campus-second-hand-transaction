package com.quguai.campustransaction.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quguai.campustransaction.product.dao.AttrDao;
import com.quguai.campustransaction.product.entity.AttrAttrgroupRelationEntity;
import com.quguai.campustransaction.product.entity.AttrEntity;
import com.quguai.campustransaction.product.entity.AttrGroupEntity;
import com.quguai.campustransaction.product.entity.CategoryEntity;
import com.quguai.campustransaction.product.service.AttrAttrgroupRelationService;
import com.quguai.campustransaction.product.service.AttrGroupService;
import com.quguai.campustransaction.product.service.AttrService;
import com.quguai.campustransaction.product.service.CategoryService;
import com.quguai.campustransaction.product.vo.AttrResponseVo;
import com.quguai.campustransaction.product.vo.AttrVo;
import com.quguai.common.constant.ProductConstant;
import com.quguai.common.utils.PageUtils;
import com.quguai.common.utils.Query;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationService relationService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        // 保存基本数据
        this.save(attrEntity);
        // 保存关联关系
        if (attr.getAttrType() == ProductConstant.AttrType.ATTR_TYPE_BASE.getCode() && attr.getAttrGroupId() != null){
            AttrAttrgroupRelationEntity entity = new AttrAttrgroupRelationEntity();
            entity.setAttrId(attrEntity.getAttrId());
            entity.setAttrGroupId(attr.getAttrGroupId());
            relationService.save(entity);
        }
    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String attrType) {
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().eq("attr_type", "base".equalsIgnoreCase(attrType)? ProductConstant.AttrType.ATTR_TYPE_BASE.getCode() : ProductConstant.AttrType.ATTR_TYPE_SALE.getCode());
        if (catelogId != 0) {
            queryWrapper.eq("catelog_id", catelogId);
        }
        String key = (String) params.get("key");
        if (StringUtils.hasText(key)) {
            queryWrapper.and(wrapper -> wrapper.eq("attr_id", key).or().like("attr_name", key));
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), queryWrapper);
        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> records = page.getRecords();

        List<AttrResponseVo> responseVos = records.stream().map(attrEntity -> {
            AttrResponseVo responseVo = new AttrResponseVo();
            BeanUtils.copyProperties(attrEntity, responseVo);
            // 1.设置分类和分组的名字
            if ("base".equalsIgnoreCase(attrType)) {
                AttrAttrgroupRelationEntity attrId = relationService.getOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
                if (attrId != null && attrId.getAttrGroupId() != null){
                    String attrGroupName = attrGroupService.getById(attrId.getAttrGroupId()).getAttrGroupName();
                    responseVo.setGroupName(attrGroupName);
                }
            }
            CategoryEntity byId = categoryService.getById(attrEntity.getCatelogId());
            if (byId != null){
                responseVo.setCatelogName(byId.getName());
            }
            return responseVo;
        }).collect(Collectors.toList());
        pageUtils.setList(responseVos);
        return pageUtils;
    }

    @Override
    public AttrResponseVo getAttrInfo(Long attrId) {
        AttrEntity attrEntity = this.getById(attrId);
        AttrResponseVo attrResponseVo = new AttrResponseVo();
        BeanUtils.copyProperties(attrEntity, attrResponseVo);
        if (attrEntity.getAttrType() == ProductConstant.AttrType.ATTR_TYPE_BASE.getCode()){
            // 获取所属分组
            AttrAttrgroupRelationEntity relationEntity = relationService.getOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if (relationEntity != null) {
                attrResponseVo.setAttrGroupId(relationEntity.getAttrGroupId());
                AttrGroupEntity entity = attrGroupService.getById(relationEntity.getAttrGroupId());
                if (entity != null) {
                    attrResponseVo.setGroupName(entity.getAttrGroupName());
                }
            }
        }
        // 获取分类信息
        List<Long> catelogPath = categoryService.findCatelogPath(attrEntity.getCatelogId());
        attrResponseVo.setCatelogPath(catelogPath);
        CategoryEntity entity = categoryService.getById(attrId);
        if (entity != null)
            attrResponseVo.setCatelogName(entity.getName());
        return attrResponseVo;
    }

    @Transactional
    @Override
    public void updateAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.updateById(attrEntity);
        if (attrEntity.getAttrType() == ProductConstant.AttrType.ATTR_TYPE_BASE.getCode()){
            // 修改分组关联
            AttrAttrgroupRelationEntity entity = new AttrAttrgroupRelationEntity();
            entity.setAttrGroupId(attr.getAttrGroupId());
            entity.setAttrId(attr.getAttrId());

            int count = relationService.count(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            if (count > 0){
                relationService.update(entity, new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
            }else {
                relationService.save(entity);
            }
        }

    }

    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> relationEntities = relationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));
        List<Long> collect = new ArrayList<>();
        if (relationEntities != null){
            collect = relationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
        }

        return collect.size() != 0 ? this.listByIds(collect) : null;
    }



    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId) {
        // 当前分组只能关联自己所属分类的里面的所有属性
        AttrGroupEntity attrGroupEntity = attrGroupService.getById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        // 当前分组只能关联别的分组没有引用的属性
        List<AttrGroupEntity> groupEntities = attrGroupService.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<Long> collect = groupEntities.stream().map(AttrGroupEntity::getAttrGroupId).collect(Collectors.toList());
        List<AttrAttrgroupRelationEntity> groupId = relationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", collect));
        List<Long> list = groupId.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());

        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId).eq("attr_type", ProductConstant.AttrType.ATTR_TYPE_BASE.getCode());
        if (list.size() != 0) {
            queryWrapper.notIn("attr_id", list);
        }
        String key = (String) params.get("key");
        if (StringUtils.hasText(key)) {
            queryWrapper.and(wrapper -> {
                wrapper.eq("attr_id", key).or().like("attr_name", key);});
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }

}