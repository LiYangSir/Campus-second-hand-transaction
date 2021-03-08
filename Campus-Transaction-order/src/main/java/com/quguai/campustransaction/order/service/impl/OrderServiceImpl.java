package com.quguai.campustransaction.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.quguai.campustransaction.order.entity.OrderItemEntity;
import com.quguai.campustransaction.order.feign.CartFeignService;
import com.quguai.campustransaction.order.feign.MemberFeignService;
import com.quguai.campustransaction.order.feign.ProductFeignService;
import com.quguai.campustransaction.order.feign.WmsFeignService;
import com.quguai.campustransaction.order.interceptor.LoginUserInterceptor;
import com.quguai.campustransaction.order.service.OrderItemService;
import com.quguai.campustransaction.order.to.OrderCreateTo;
import com.quguai.campustransaction.order.vo.*;
import com.quguai.common.constant.OrderConstant;
import com.quguai.common.exception.NoStockException;
import com.quguai.common.utils.R;
import com.quguai.common.vo.MemberResponseVo;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quguai.common.utils.PageUtils;
import com.quguai.common.utils.Query;

import com.quguai.campustransaction.order.dao.OrderDao;
import com.quguai.campustransaction.order.entity.OrderEntity;
import com.quguai.campustransaction.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private CartFeignService cartFeignService;

    @Autowired
    private ThreadPoolExecutor executor;

    @Autowired
    private WmsFeignService wmsFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private OrderItemService orderItemService;

    private ThreadLocal<OrderSubmitVo> confirmVoThreadLocal = new ThreadLocal<>();

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 异步编排
     * @return
     */
    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = new OrderConfirmVo();
        MemberResponseVo loginUser = LoginUserInterceptor.loginUser.get();

        // 每一个线程都共享数据
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        // 远程调用获取所有的收获列表
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            // 共享奥主线程中的数据
            RequestContextHolder.setRequestAttributes(attributes);
            List<MemberAddressVo> address = memberFeignService.getAddress(loginUser.getId());
            confirmVo.setAddress(address);
        }, executor);

        //远程获取购物车所有选中的购物项
        CompletableFuture<Void> cartFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
            confirmVo.setOrderItemVos(currentUserCartItems);
        }).thenRunAsync(() -> {
            List<OrderItemVo> items = confirmVo.getOrderItemVos();
            List<Long> collect = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            List<SkuStockVo> skuStocks = wmsFeignService.getSkuHasStock(collect).getData(new TypeReference<List<SkuStockVo>>() {});
            if (skuStocks != null) {
                Map<Long, Boolean> booleanMap = skuStocks.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                confirmVo.setStocks(booleanMap);
            }
        }, executor);

        // 查询用户积分
        confirmVo.setIntegration(loginUser.getIntegration());
        // TODO 防重令牌
        String uuid = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + loginUser.getId(), uuid, 30, TimeUnit.MINUTES);
        confirmVo.setOrderToken(uuid);

        CompletableFuture.allOf(addressFuture, cartFuture).get();
        return confirmVo;
    }

    @GlobalTransactional
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        confirmVoThreadLocal.set(vo);
        responseVo.setCode(0);
        // 验证令牌 [令牌的对比和删除必须保证完整性]
        String token = vo.getOrderToken();
        MemberResponseVo loginUser = LoginUserInterceptor.loginUser.get();
        // 原子删除锁
        Long res = deleteToken(token, loginUser);
        // 0 代表失败，1 代表成功
        if (res != null && res == 1L) {
            // 验证成功
            OrderCreateTo order = createOrder();
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                // 保存订单
                saveOrder(order);
                // 锁定库存
                if (lockStock(order)) {
                    responseVo.setOrder(order.getOrder());
                    return responseVo;
                } else {
                    // 保存订单- 锁定库存(远程)- 扣减积分(远程),
                    // 库存的失败可以通过异常机制回滚订单，单向的，无法回滚库存，例如 扣减积分出现问题
                    // 当调用远程服务出现加异常，超时等问题，锁定库存成功，但是还是会有一场的出现，本质就是单向问题
                    throw new NoStockException();
                }
            } else {
                responseVo.setCode(2);
                return responseVo;
            }
        } else {
            responseVo.setCode(1);
        }
        return responseVo;
    }

    private Long deleteToken(String token, MemberResponseVo loginUser) {
        String key = OrderConstant.USER_ORDER_TOKEN_PREFIX + loginUser.getId();
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        return redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(key), token);
    }

    private boolean lockStock(OrderCreateTo order) {
        // 库存锁定， 只要有异常就会滚数据
        WareSkuLockVo lockVo = new WareSkuLockVo();
        List<OrderItemVo> collect = order.getOrderItems().stream().map(orderItemEntity -> {
            OrderItemVo orderItemVo = new OrderItemVo();
            orderItemVo.setSkuId(orderItemEntity.getSkuId());
            orderItemVo.setCount(orderItemEntity.getSkuQuantity());
            orderItemVo.setTitle(orderItemEntity.getSkuName());
            return orderItemVo;
        }).collect(Collectors.toList());
        lockVo.setLock(collect);
        lockVo.setOrderSn(order.getOrder().getOrderSn());
        R r = wmsFeignService.orderLockStock(lockVo);
        return r.getCode() == 0;
    }

    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);
        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);
    }

    private OrderCreateTo createOrder(){
        OrderCreateTo orderCreateTo = new OrderCreateTo();
        // 1.创建订单号
        String orderSn = IdWorker.getTimeId();
        OrderEntity entity = buildOrder(orderSn);
        orderCreateTo.setOrder(entity);
        // 2.获取所有的订单项
        List<OrderItemEntity> itemEntities = buildOrderItems(orderSn);
        orderCreateTo.setOrderItems(itemEntities);

        // 3.计算价格相关
        computePrice(entity, itemEntities);

        orderCreateTo.setOrder(entity);
        orderCreateTo.setOrderItems(itemEntities);
        return orderCreateTo;
    }

    private void computePrice(OrderEntity entity, List<OrderItemEntity> itemEntities) {
        BigDecimal total = new BigDecimal(0);
        for (OrderItemEntity itemEntity : itemEntities) {
            BigDecimal realAmount = itemEntity.getRealAmount();
            total = total.add(realAmount);
        }
        entity.setTotalAmount(total);
        entity.setPayAmount(total.add(entity.getFreightAmount()));
    }

    private OrderEntity buildOrder(String orderSn) {
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(orderSn);
        entity.setMemberId(LoginUserInterceptor.loginUser.get().getId());
        // 获取收货地址信息
        OrderSubmitVo submitVo = confirmVoThreadLocal.get();
        // 远程获取收货地址信息
        R fare = wmsFeignService.getFare(submitVo.getAddrId());
        FareVo fareVo = fare.getData(new TypeReference<FareVo>(){});
        // 设置运费信息
        entity.setFreightAmount(fareVo.getFare());
        entity.setReceiverCity(fareVo.getAddressVo().getCity());
        entity.setReceiverDetailAddress(fareVo.getAddressVo().getDetailAddress());
        entity.setReceiverName(fareVo.getAddressVo().getName());
        entity.setReceiverPhone(fareVo.getAddressVo().getPhone());
        entity.setReceiverPostCode(fareVo.getAddressVo().getPostCode());
        entity.setReceiverProvince(fareVo.getAddressVo().getProvince());
        entity.setReceiverRegion(fareVo.getAddressVo().getRegion());
        return entity;
    }

    // 构建订单项
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if (currentUserCartItems != null && currentUserCartItems.size() > 0) {
            return currentUserCartItems.stream().map(orderItemVo -> {
                OrderItemEntity itemEntity = buildOrderItem(orderItemVo);
                itemEntity.setOrderSn(orderSn);
                return itemEntity;
            }).collect(Collectors.toList());
        }
        return null;
    }

    private OrderItemEntity buildOrderItem(OrderItemVo orderItemVo) {
        OrderItemEntity entity = new OrderItemEntity();
        // 1.订单信息
        // 2.商品的SPU信息
        Long skuId = orderItemVo.getSkuId();
        SpuInfoVo spuInfoVo = productFeignService.getSpuInfoBySkuId(skuId).getData(new TypeReference<SpuInfoVo>() {});
        entity.setSpuId(spuInfoVo.getId());
        entity.setSpuBrand(spuInfoVo.getBrandId().toString());
        entity.setSpuName(spuInfoVo.getSpuName());
        entity.setCategoryId(spuInfoVo.getCatalogId());
        // 3.商品的sku信息
        entity.setSkuId(orderItemVo.getSkuId());
        entity.setSkuName(orderItemVo.getTitle());
        entity.setSkuPic(orderItemVo.getImage());
        entity.setSkuPrice(orderItemVo.getPrice());
        String skuAttr = StringUtils.collectionToDelimitedString(orderItemVo.getSkuAttrs(), ";");
        entity.setSkuAttrsVals(skuAttr);
        entity.setSkuQuantity(orderItemVo.getCount());

        // 4.积分信息
        entity.setGiftIntegration(orderItemVo.getPrice().intValue());
        entity.setGiftGrowth(orderItemVo.getPrice().intValue());

        // 5.订单项的价格信息
        entity.setPromotionAmount(new BigDecimal(0));
        entity.setCouponAmount(new BigDecimal(0));
        entity.setIntegrationAmount(new BigDecimal(0));
        BigDecimal origin = entity.getSkuPrice().multiply(new BigDecimal(entity.getSkuQuantity()));
        BigDecimal realPrice = origin.subtract(entity.getCouponAmount()).subtract(entity.getIntegrationAmount()).subtract(entity.getPromotionAmount());
        entity.setRealAmount(realPrice);
        return entity;
    }

}