package com.quguai.campustransaction.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quguai.campustransaction.product.dao.CategoryDao;
import com.quguai.campustransaction.product.entity.CategoryEntity;
import com.quguai.campustransaction.product.service.CategoryBrandRelationService;
import com.quguai.campustransaction.product.service.CategoryService;
import com.quguai.campustransaction.product.vo.Catelog2Vo;
import com.quguai.common.utils.PageUtils;
import com.quguai.common.utils.Query;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
@CacheConfig(cacheNames = "catelog")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {


    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void removeMenuByIds(List<Long> asList) {
        // TODO 检查当前删除的菜单是否被别的地方所引用
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public List<Long> findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        CategoryEntity entity = this.getById(catelogId);
        paths.add(entity.getCatId());
        while (entity.getParentCid() != 0) {
            paths.add(entity.getParentCid());
            entity = this.getById(entity.getParentCid());
        }
        Collections.reverse(paths);
        return paths;
    }

    /**
     * 失效模式进行删除
     * 双写模式：@CachePut
     * @param :category
     */
    @Transactional
    @Override
//    @Caching(evict = {
//            @CacheEvict(key = "'getLevelFirstCategories'"),
//            @CacheEvict(key = "'getCatelogJsonFromCache'")
//    })
    @CacheEvict(allEntries = true)
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        if (StringUtils.hasLength(category.getName())) {
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
            // TODO 更新其他表中数据
        }
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        List<CategoryEntity> entities = baseMapper.selectList(null);
        return entities.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                .map(categoryEntity -> {
                    categoryEntity.setChildren(getChildren(categoryEntity, entities));
                    return categoryEntity;
                })
                .sorted(Comparator.comparingInt(o -> (o.getSort() == null ? 0 : o.getSort())))
                .collect(Collectors.toList());
    }

    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        return all.stream().filter(entity -> entity.getParentCid().equals(root.getCatId()))
                .map(entity -> {
                    entity.setChildren(getChildren(entity, all));
                    return entity;
                })
                .sorted(Comparator.comparingInt(o -> (o.getSort() == null ? 0 : o.getSort())))
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(key = "#root.methodName", sync = true)
    public List<CategoryEntity> getLevelFirstCategories() {
        return this.list(new QueryWrapper<CategoryEntity>().eq("cat_level", 1));
    }

    @Override
    @Cacheable(key = "#root.methodName")
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromCache() {

        List<CategoryEntity> list = this.list();
        Map<String, List<Catelog2Vo>> listMap = list.stream().filter(entity -> entity.getParentCid() == 0).collect(Collectors.toMap(v -> v.getCatId().toString(),
                v -> list.stream()
                        .filter(entity -> entity.getParentCid().equals(v.getCatId()))
                        .map(entity -> {
                            Catelog2Vo catelog2Vo = new Catelog2Vo(entity.getParentCid().toString(), entity.getCatId().toString(), entity.getName(), null);
                            List<Catelog2Vo.Catelog3Vo> collect = list.stream()
                                    .filter(entity1 -> entity1.getParentCid().equals(entity.getCatId()))
                                    .map(entity1 -> new Catelog2Vo.Catelog3Vo(entity1.getParentCid().toString(), entity1.getCatId().toString(), entity1.getName()))
                                    .collect(Collectors.toList());
                            catelog2Vo.setCatalog3List(collect);
                            return catelog2Vo;
                        }).collect(Collectors.toList())));
        return listMap;
    }

    public Map<String, List<Catelog2Vo>> getCatelogJsonFromCache2() {
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
        String catelogJson = opsForValue.get("catelog_json");
        if (!StringUtils.hasText(catelogJson)) {
            /*
             * 1. 加锁解决缓存击穿问题
             * 2. 存储null解决缓存穿透的问题
             * 3. 加随机值解决缓存雪崩问题
             * 最主要是保证：确认缓存是否包含，查询数据库，放入缓存整体的原子性
             */
            return getCatelogJSONFromDbWithRedisLock();
        }
        return JSON.parseObject(catelogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
    }

    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJSONFromDbWithLocalLock() {
        // 得到锁以后要查看缓存当中是否包含
        synchronized (this) {
            ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
            return getDataFromDbOrCache(opsForValue);
        }

    }

    public Map<String, List<Catelog2Vo>> getCatelogJSONFromDbWithRedisLock() {
        ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
        String uuid = UUID.randomUUID().toString();
        Boolean ifAbsent = opsForValue.setIfAbsent("lock", uuid, 30, TimeUnit.SECONDS);
        // 自旋锁
        while (ifAbsent != null && !ifAbsent) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {

            }
            ifAbsent = opsForValue.setIfAbsent("lock", uuid, 30, TimeUnit.SECONDS);
        }
        try {
            Map<String, List<Catelog2Vo>> cache = getDataFromDbOrCache(opsForValue);
            return cache;
        } finally {
            // 释放锁
            String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
            Long res = stringRedisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList("lock"), uuid);
        }

        // 加锁成功
        //if (uuid.equals(opsForValue.get("lock"))){
        //    // 释放锁
        //    stringRedisTemplate.delete("lock");
        //}

    }

    public Map<String, List<Catelog2Vo>> getCatelogJSONFromDbWithRedissonLock() {
        RLock lock = redissonClient.getLock("catelog_json_lock");
        lock.lock();
        try {
            ValueOperations<String, String> opsForValue = stringRedisTemplate.opsForValue();
            return getDataFromDbOrCache(opsForValue);
        } finally {
            lock.unlock();
        }
    }

    private Map<String, List<Catelog2Vo>> getDataFromDbOrCache(ValueOperations<String, String> opsForValue) {
        String s = opsForValue.get("catelog_json");
        if (s != null) {
            return JSON.parseObject(s, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
        }
        List<CategoryEntity> list = this.list();
        Map<String, List<Catelog2Vo>> listMap = list.stream().filter(entity -> entity.getParentCid() == 0).collect(Collectors.toMap(v -> v.getCatId().toString(),
                v -> list.stream()
                        .filter(entity -> entity.getParentCid().equals(v.getCatId()))
                        .map(entity -> {
                            Catelog2Vo catelog2Vo = new Catelog2Vo(entity.getParentCid().toString(), entity.getCatId().toString(), entity.getName(), null);
                            List<Catelog2Vo.Catelog3Vo> collect = list.stream()
                                    .filter(entity1 -> entity1.getParentCid().equals(entity.getCatId()))
                                    .map(entity1 -> new Catelog2Vo.Catelog3Vo(entity1.getParentCid().toString(), entity1.getCatId().toString(), entity1.getName()))
                                    .collect(Collectors.toList());
                            catelog2Vo.setCatalog3List(collect);
                            return catelog2Vo;
                        }).collect(Collectors.toList())));

        // 解决缓存穿透的问题：保存null并设置过期时间
        // 解决缓存雪崩的问题：设置随机的过期时间
        // 解决缓存击穿的问题：加锁
        opsForValue.set("catelog_json", JSON.toJSONString(listMap), 1, TimeUnit.DAYS);
        System.out.println("保存到缓存");
        return listMap;
    }
}