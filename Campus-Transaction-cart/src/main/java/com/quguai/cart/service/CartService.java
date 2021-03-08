package com.quguai.cart.service;

import com.quguai.cart.vo.Cart;
import com.quguai.cart.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface CartService {
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    CartItem getCartItem(Long skuId);

    Cart getCart() throws ExecutionException, InterruptedException;

    void clearCart(String cartKey);

    void changeCartCheck(Long skuId, Integer check);

    void changeCartCount(Long skuId, Integer num);

    void deleteCart(Long skuId);

    List<CartItem> getUserCartItems();
}
