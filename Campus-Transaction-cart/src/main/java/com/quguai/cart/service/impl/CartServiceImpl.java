package com.quguai.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.quguai.cart.feign.ProductFeignService;
import com.quguai.cart.interceptor.CartInterceptor;
import com.quguai.cart.service.CartService;
import com.quguai.cart.vo.Cart;
import com.quguai.cart.vo.CartItem;
import com.quguai.cart.vo.SkuInfoVo;
import com.quguai.cart.vo.UserInfoTo;
import com.quguai.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.util.ListUtils;
import sun.print.PeekGraphics;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private ThreadPoolExecutor executor;

    private final String CART_PREFIX = "campus:cart:";

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String o = (String) cartOps.get(skuId.toString());
        if (StringUtils.hasText(o)) {
            // 只需要修改count
            CartItem cartItem = JSON.parseObject(o, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        } else {
            // 添加新商品到购物车
            CartItem cartItem = new CartItem();
            // 1. 远程查询当前添加商品的信息
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                R info = productFeignService.info(skuId);
                SkuInfoVo skuInfo = info.getData("skuInfo", new TypeReference<SkuInfoVo>() {});
                cartItem.setSkuId(cartItem.getSkuId());
                cartItem.setImage(skuInfo.getSkuDefaultImg());
                cartItem.setCheck(true);
                cartItem.setCount(num);
                cartItem.setTitle(skuInfo.getSkuTitle());
                cartItem.setPrice(skuInfo.getPrice());
            }, executor);

            CompletableFuture<Void> attrsTask = CompletableFuture.runAsync(() -> {
                List<String> skuSaleAttr = productFeignService.getSkuSaleAttr(skuId);
                cartItem.setSkuAttrs(skuSaleAttr);
            }, executor);

            CompletableFuture.allOf(getSkuInfoTask, attrsTask).get();
            // 放入redis
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        }
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String s = (String) cartOps.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(s, CartItem.class);
        return cartItem;
    }

    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        Cart cart = new Cart();
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() != null) {
            // 登陆的购物车并合并
            String userId = CART_PREFIX + userInfoTo.getUserId();
            String userKey = CART_PREFIX + userInfoTo.getUserKey();
            List<CartItem> temporary = getCartItems(userKey);
            if (!ListUtils.isEmpty(temporary)) {
                // 需要进行合并操作
                for (CartItem cartItem : temporary) {
                    addToCart(cartItem.getSkuId(), cartItem.getCount()); // 有追加操作
                }
                // 删除操作
                clearCart(userKey);
            }
            // 获取登陆后的购物车
            List<CartItem> cartItems = getCartItems(userId);
            cart.setItems(cartItems);
        } else {
            // 没有进行登录
            List<CartItem> cartItems = getCartItems(CART_PREFIX + userInfoTo.getUserKey());
            cart.setItems(cartItems);
        }
        return cart;

    }

    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey = "";
        if (userInfoTo.getUserId() != null) {
            // 用户已经登陆
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        } else {
            // 用户未登录
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }
        return redisTemplate.boundHashOps(cartKey);
    }

    private List<CartItem> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> hashOperations = redisTemplate.boundHashOps(cartKey);
        List<Object> values = hashOperations.values();
        if (!ListUtils.isEmpty(values)) {
           return values.stream().map(o -> JSON.parseObject(o.toString(), CartItem.class)).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public void clearCart(String cartKey){
        redisTemplate.delete(cartKey);
    }

    @Override
    public void changeCartCheck(Long skuId, Integer check) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(check == 1);
        getCartOps().put(skuId, JSON.toJSONString(cartItem));
    }

    @Override
    public void changeCartCount(Long skuId, Integer num) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        getCartOps().put(skuId, JSON.toJSONString(cartItem));
    }

    @Override
    public void deleteCart(Long skuId) {
        getCartOps().delete(skuId);
    }

    @Override
    public List<CartItem> getUserCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() == null) {
            return null;
        }
        String cartKey = CART_PREFIX + userInfoTo.getUserKey();
        List<CartItem> cartItems = getCartItems(cartKey);
        if (cartItems != null) {
            cartItems = cartItems.stream()
                    .filter(CartItem::getCheck)
                    .peek(cartItem -> {
                        BigDecimal price = productFeignService.getPrice(cartItem.getSkuId());
                        cartItem.setPrice(price);
                    })
                    .collect(Collectors.toList());
        }
        return cartItems;
    }
}
