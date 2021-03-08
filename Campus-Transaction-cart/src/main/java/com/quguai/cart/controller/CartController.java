package com.quguai.cart.controller;

import com.quguai.cart.interceptor.CartInterceptor;
import com.quguai.cart.service.CartService;
import com.quguai.cart.vo.Cart;
import com.quguai.cart.vo.CartItem;
import com.quguai.cart.vo.UserInfoTo;
import com.quguai.common.constant.AuthServerConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @ResponseBody
    @GetMapping("/currentUserCartItems")
    public List<CartItem> getCurrentUserCartItems(){
        return cartService.getUserCartItems();
    }

    @GetMapping("/cart.html")
    public String cartListPage(Model model) throws ExecutionException, InterruptedException {

        Cart cart = cartService.getCart();
        model.addAttribute("cart", cart);
        return "cartList";
    }

    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num,
                            RedirectAttributes attributes) throws ExecutionException, InterruptedException {
        cartService.addToCart(skuId, num);
        attributes.addAttribute("skuId", skuId);
        return "redirect:http://cart.campus.com/addToCartSuccess.html";
    }

    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") Long skuId, Model model) {
        CartItem cartItem = cartService.getCartItem(skuId);
        model.addAttribute("item", cartItem);
        return "success";
    }

    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId")Long skuId,@RequestParam("check") Integer check) {
        cartService.changeCartCheck(skuId, check);
        return "redirect:http://cart.campus.com/cart.html";
    }

    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId")Long skuId,@RequestParam("num") Integer num) {
        cartService.changeCartCount(skuId, num);
        return "redirect:http://cart.campus.com/cart.html";
    }

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId")Long skuId) {
        cartService.deleteCart(skuId);
        return "redirect:http://cart.campus.com/cart.html";
    }
}
