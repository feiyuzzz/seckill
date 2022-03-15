package com.ms.seckill.controller;


import com.ms.seckill.pojo.User;
import com.ms.seckill.rabbitmq.MQSender;
import com.ms.seckill.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/user")
public class UserController {

/*
    @Autowired
    private MQSender mqSender;


    @RequestMapping("/info")
    @ResponseBody
    public RespBean info(User user) {
        return RespBean.success(user);
    }

    *//**
     * 测试RabbitMQ消息
     *//*
    @RequestMapping("/mq")
    @ResponseBody
    public void mq() {
        mqSender.send("Hello");
    }

    *//**
     * 测试RabbitMQ消息
     *//*
    @RequestMapping("/mq/fanout")
    @ResponseBody
    public void mq01() {
        mqSender.send("Hello Fanout模式");
    }


    @RequestMapping("/mq/direct01")
    @ResponseBody
    public void mq02() {
        mqSender.send01("Hello Direct Red");
    }

    @RequestMapping("/mq/direct02")
    @ResponseBody
    public void mq03() {
        mqSender.send02("Hello Direct Green");
    }


    @RequestMapping("/mq/topic01")
    @ResponseBody
    public void mq04() {
        mqSender.send01Topic("Hello Topic 01");
    }


    @RequestMapping("/mq/topic02")
    @ResponseBody
    public void mq05() {
        mqSender.send02Topic("Hello Topic 02");
    }

    @RequestMapping("/mq/header01")
    @ResponseBody
    public void mq06() {
        mqSender.send01Header("Hello Header");
    }

    @RequestMapping("/mq/header02")
    @ResponseBody
    public void mq07() {
        mqSender.send02Header("Hello Header 01");
    }*/

}
