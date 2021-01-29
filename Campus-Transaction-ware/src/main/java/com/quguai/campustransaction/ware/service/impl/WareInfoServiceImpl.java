package com.quguai.campustransaction.ware.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quguai.common.utils.PageUtils;
import com.quguai.common.utils.Query;

import com.quguai.campustransaction.ware.dao.WareInfoDao;
import com.quguai.campustransaction.ware.entity.WareInfoEntity;
import com.quguai.campustransaction.ware.service.WareInfoService;
import org.springframework.util.StringUtils;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {

        String key = (String) params.get("key");
        QueryWrapper<WareInfoEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(key)) {
            queryWrapper.eq("id", key).or()
                    .like("name", key).or()
                    .like("address", key).or()
                    .like("areacode", key).or();
        }

        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

}