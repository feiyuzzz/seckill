package com.ms.seckill.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class MQSender {


    @Autowired
    private RabbitTemplate rabbitTemplate;

 /*   public void send(Object msg) {
        log.info("发送消息：" + msg);
        rabbitTemplate.convertAndSend("fanoutExchange", "", msg);
    }

    public void send01(Object msg) {
        log.info("发送消息：" + msg);
        rabbitTemplate.convertAndSend("directExchange", "queue.red", msg);
    }

    public void send02(Object msg) {
        log.info("发送消息：" + msg);
        rabbitTemplate.convertAndSend("directExchange", "queue.green", msg);
    }

    public void send01Topic(Object msg) {
        log.info("发送消息(一个Queue接收)：" + msg);
        rabbitTemplate.convertAndSend("topicExchange", "queue.red.message", msg);
    }

    public void send02Topic(Object msg) {
        log.info("发送消息(两个Queue接收)：" + msg);
        rabbitTemplate.convertAndSend("topicExchange", "topic.queue.red", msg);
    }

    public void send01Header(String msg) {
        log.info("发送消息(两个Queue接收)：" + msg);
        MessageProperties properties = new MessageProperties();
        properties.setHeader("color", "red");
        properties.setHeader("speed", "low");
        Message message = new Message(msg.getBytes(), properties);
        rabbitTemplate.convertAndSend("headerExchange", "", message);
    }

    public void send02Header(String msg) {
        log.info("发送消息(被Queue01接受):" + msg);
        MessageProperties properties = new MessageProperties();
        properties.setHeader("color", "red");
        properties.setHeader("speed", "normal");
        Message message = new Message(msg.getBytes(), properties);
        rabbitTemplate.convertAndSend("headerExchange", "", message);
    }*/

    /**
     * 发送描述信息
     * @param msg
     */
    public void sendSecKillMessage(String msg){
        log.info("发送信息:{}",msg);
        rabbitTemplate.convertAndSend("secKillExchange","secKill.message",msg);
    }

}
