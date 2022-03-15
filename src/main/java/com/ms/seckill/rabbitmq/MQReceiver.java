package com.ms.seckill.rabbitmq;

import com.ms.seckill.pojo.SeckKillMessage;
import com.ms.seckill.pojo.SeckillOrder;
import com.ms.seckill.pojo.User;
import com.ms.seckill.service.IGoodsService;
import com.ms.seckill.service.IOrderService;
import com.ms.seckill.utils.JSONUtil;
import com.ms.seckill.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MQReceiver {


    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IOrderService orderService;

    /**
     * 下单操作
     */
    @RabbitListener(queues = "secKillQueue")
    public void receive(String msg) {
        log.info("接收到消息：{}", msg);
        SeckKillMessage seckKillMessage = JSONUtil.jsonStr2Object(msg, SeckKillMessage.class);
        Long goodsId = seckKillMessage.getGoodsId();
        User user = seckKillMessage.getUser();
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        if (goodsVo.getStockCount() < 1) {
            return;
        }
        // 判断是否重复抢购
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if (seckillOrder != null) {
            return;
        }
        //下单操作
        orderService.seckill(user,goodsVo);
    }

   /* @RabbitListener(queues = "queue")
    public void receive(Object msg) {
        log.info("接收消息：" + msg);
    }

    @RabbitListener(queues = "queue_fanout01")
    public void receive01(Object msg) {
        log.info("QUEUE FANOUT O1 接收消息：" + msg);
    }

    @RabbitListener(queues = "queue_fanout02")
    public void receive02(Object msg) {
        log.info("QUEUE FANOUT 02 接收消息：" + msg);
    }


    @RabbitListener(queues = "queue_direct01")
    public void receive03(Object msg) {
        log.info("QUEUE DIRECT 01 接收消息：" + msg);
    }

    @RabbitListener(queues = "queue_direct02")
    public void receive04(Object msg) {
        log.info("QUEUE DIRECT 02 接收消息：" + msg);
    }

    @RabbitListener(queues = "queue_topic01")
    public void receive05(Object msg) {
        log.info("QUEUE TOPIC 01 接收消息：" + msg);
    }

    @RabbitListener(queues = "queue_topic02")
    public void receive06(Object msg) {
        log.info("QUEUE TOPIC 02 接收消息：" + msg);
    }

    @RabbitListener(queues = "queue_header01")
    public void receive07(Message msg) {
        log.info("QUEUE HEADER 01 接收对象：" + msg);
        log.info("QUEUE HEADER 01 接收消息："+new String(msg.getBody()));
    }

    @RabbitListener(queues = "queue_header02")
    public void receive08(Message msg) {
        log.info("QUEUE HEADER 02 接收对象：" + msg);
        log.info("QUEUE HEADER 02 接收消息："+new String(msg.getBody()));
    }*/
}
