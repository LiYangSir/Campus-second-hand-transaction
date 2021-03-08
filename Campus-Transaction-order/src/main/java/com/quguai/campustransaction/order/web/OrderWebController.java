package com.quguai.campustransaction.order.web;

import com.quguai.campustransaction.order.service.OrderService;
import com.quguai.campustransaction.order.vo.OrderConfirmVo;
import com.quguai.campustransaction.order.vo.OrderSubmitVo;
import com.quguai.campustransaction.order.vo.SubmitOrderResponseVo;
import com.quguai.common.constant.AuthServerConstant;
import com.quguai.common.exception.NoStockException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.concurrent.ExecutionException;

@Controller
public class OrderWebController {

    @Autowired
    private OrderService orderService;
    
    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo confirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData", confirmVo);
        return "confirm";
    }

    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes attributes) {
        try {
            SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);
            if (responseVo.getCode() == 0) {
                // 成功
                model.addAttribute("submitOrderResponse", responseVo);
                return "pay";
            } else {
                attributes.addFlashAttribute("msg", responseVo.getCode());
                return "redirect:http://order.campus.com/toTrade";
            }
        } catch (NoStockException e) {
            attributes.addFlashAttribute("msg", e.getMessage());
            return "redirect:http://order.campus.com/toTrade";
        }

    }
}
