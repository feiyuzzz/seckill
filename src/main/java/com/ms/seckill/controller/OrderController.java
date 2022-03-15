package com.ms.seckill.controller;


import com.ms.seckill.pojo.User;
import com.ms.seckill.service.IOrderService;
import com.ms.seckill.vo.OrderDetailVo;
import com.ms.seckill.vo.RespBean;
import com.ms.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    @RequestMapping("/detail")
    @ResponseBody
    public RespBean detail(User user,Long orderId){
        if (null == user){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        OrderDetailVo detail = orderService.detail(orderId);
        return RespBean.success(detail);
    }

}
