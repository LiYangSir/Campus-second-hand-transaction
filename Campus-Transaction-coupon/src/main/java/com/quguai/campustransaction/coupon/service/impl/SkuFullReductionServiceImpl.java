package com.quguai.campustransaction.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quguai.campustransaction.coupon.dao.SkuFullReductionDao;
import com.quguai.campustransaction.coupon.entity.MemberPriceEntity;
import com.quguai.campustransaction.coupon.entity.SkuFullReductionEntity;
import com.quguai.campustransaction.coupon.entity.SkuLadderEntity;
import com.quguai.campustransaction.coupon.service.MemberPriceService;
import com.quguai.campustransaction.coupon.service.SkuFullReductionService;
import com.quguai.campustransaction.coupon.service.SkuLadderService;
import com.quguai.common.to.MemberPrice;
import com.quguai.common.to.SkuReductionTo;
import com.quguai.common.utils.PageUtils;
import com.quguai.common.utils.Query;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    private SkuLadderService skuLadderService;

    @Autowired
    private MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        //1. 保存折扣
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(skuReductionTo, skuLadderEntity);
        skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());
        if (skuLadderEntity.getFullCount() > 0)
            skuLadderService.save(skuLadderEntity);

        // 保存满减信息
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTo, skuFullReductionEntity);
        skuFullReductionEntity.setAddOther(skuReductionTo.getCountStatus());
        if (skuFullReductionEntity.getFullPrice().compareTo(BigDecimal.ZERO) > 0)
            this.save(skuFullReductionEntity);

        // 会员价格
        List<MemberPrice> memberPrice = skuReductionTo.getMemberPrice();
        List<MemberPriceEntity> collect = memberPrice.stream()
                .filter(member -> member.getPrice().compareTo(BigDecimal.ZERO) > 0)
                .map(member -> {
                    MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
                    memberPriceEntity.setSkuId(skuReductionTo.getSkuId());
                    memberPriceEntity.setMemberLevelName(member.getName());
                    memberPriceEntity.setMemberPrice(member.getPrice());
                    memberPriceEntity.setMemberLevelId(member.getId());
                    memberPriceEntity.setAddOther(1);
                    return memberPriceEntity;
                }).collect(Collectors.toList());

        memberPriceService.saveBatch(collect);
    }

}