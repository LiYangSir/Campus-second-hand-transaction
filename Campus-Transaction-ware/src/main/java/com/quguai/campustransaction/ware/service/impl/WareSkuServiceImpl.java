package com.quguai.campustransaction.ware.service.impl;

import com.quguai.campustransaction.ware.exception.NoStockException;
import com.quguai.campustransaction.ware.feign.ProductFeignService;
import com.quguai.campustransaction.ware.vo.OrderItemVo;
import com.quguai.campustransaction.ware.vo.SkuHasStockVo;
import com.quguai.campustransaction.ware.vo.WareSkuLockVo;
import com.quguai.common.utils.R;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quguai.common.utils.PageUtils;
import com.quguai.common.utils.Query;

import com.quguai.campustransaction.ware.dao.WareSkuDao;
import com.quguai.campustransaction.ware.entity.WareSkuEntity;
import com.quguai.campustransaction.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private ProductFeignService productFeignService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (StringUtils.hasText(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }
        String wareId = (String) params.get("wareId");
        if (StringUtils.hasText(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        Integer integer = baseMapper.selectCount(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (integer != 0)
            baseMapper.addStock(skuId, wareId, skuNum);
        else{
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStockLocked(0);
            R info = productFeignService.info(skuId);
            // 失败以后无需回滚
            // 1. catch 无操作
            // 2. TODO 还可以有什么方法
            try {
                if (info.getCode() == 0) {
                    Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");
                    wareSkuEntity.setSkuName((String) data.get("skuName"));
                }
            }catch (Exception e){

            }

            baseMapper.insert(wareSkuEntity);
        }
    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {
        List<SkuHasStockVo> collect = skuIds.stream().map(skuId -> {
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
            Long count = baseMapper.getSkuStock(skuId);
            skuHasStockVo.setSkuId(skuId);
            skuHasStockVo.setHasStock(count != null && count > 0);
            return skuHasStockVo;
        }).collect(Collectors.toList());
        System.out.println(collect);
        return collect;
    }

    @Transactional(rollbackFor = NoStockException.class)  // 默认运行时异常都要回滚
    @Override
    public Boolean orderLockStock(WareSkuLockVo vo) {
        List<OrderItemVo> locks = vo.getLock();
        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            skuWareHasStock.setSkuId(skuId);
            skuWareHasStock.setNum(item.getCount());
            List<Long> wareIds = this.baseMapper.listWareIdHasSkuStock(skuId);
            skuWareHasStock.setWareId(wareIds);
            return skuWareHasStock;
        }).collect(Collectors.toList());

        for (SkuWareHasStock stock : collect) {
            Boolean skuStock = false;
            Long skuId = stock.getSkuId();
            List<Long> wareIds = stock.getWareId();
            if (wareIds == null || wareIds.size() == 0) {
                throw new NoStockException(skuId);
            }
            for (Long wareId : wareIds) {
                // 成功返回1
                Long count = this.baseMapper.lockSkuStock(skuId, wareId, stock.getNum());
                if (count == 1) {
                    skuStock = true;
                    break;
                } else {

                }
            }
            if (!skuStock) {
                // 当前商品所有仓库没有锁住
                throw new NoStockException(skuId);
            }
        }
        // 全部锁定成功
        return true;
    }

    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }


}