package com.ms.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ms.seckill.exception.GlobalException;
import com.ms.seckill.mapper.OrderMapper;
import com.ms.seckill.pojo.*;
import com.ms.seckill.service.IGoodsService;
import com.ms.seckill.service.IOrderService;
import com.ms.seckill.service.ISeckillGoodsService;
import com.ms.seckill.service.ISeckillOrderService;
import com.ms.seckill.utils.MD5Util;
import com.ms.seckill.utils.UUIDUtil;
import com.ms.seckill.vo.GoodsVo;
import com.ms.seckill.vo.OrderDetailVo;
import com.ms.seckill.vo.RespBean;
import com.ms.seckill.vo.RespBeanEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j(topic = "e")
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {


    @Autowired
    private ISeckillGoodsService seckillGoodsService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ISeckillOrderService seckillOrderService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IGoodsService goodsService;

    /**
     * 秒杀
     *
     * @param user
     * @param goodsVo
     * @return
     */
    @Transactional
    @Override
    public Order seckill(User user, GoodsVo goodsVo) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //秒杀商品表减库存
        SeckillGoods seckillGoods = seckillGoodsService.getOne(new QueryWrapper<SeckillGoods>()
                .eq("goods_id", goodsVo.getId()));
        seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
//        seckillGoodsService.updateById(seckillGoods);

        // 更新前判断库存是否大于0
        boolean result = seckillGoodsService.update(new UpdateWrapper<SeckillGoods>()
                .setSql("stock_count = stock_count-1")
                .eq("goods_id", goodsVo.getId())
                .gt("stock_count", 0));
        if (seckillGoods.getStockCount() < 1) {
            valueOperations.set("isStockEmpty:" + goodsVo.getId(), "0");
            return null;
        }

        //生成订单
        Order order = new Order();
        order.setUserId(user.getId());
        order.setGoodsId(goodsVo.getId());
        order.setDeliveryAddrId(0L);
        order.setGoodsName(goodsVo.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(seckillGoods.getSeckillPrice());
        order.setOrderChannel(1);
        order.setStatus(0);
        order.setCreateDate(new Date());
        orderMapper.insert(order);

        //生成秒杀订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setUserId(user.getId());
        seckillOrder.setGoodsId(order.getGoodsId());
        seckillOrder.setOrderId(order.getId());
        seckillOrderService.save(seckillOrder);

        valueOperations.set("order:" + user.getId() + ":" + goodsVo.getId(), seckillOrder);

        return order;
    }

    @Override
    public OrderDetailVo detail(Long orderId) {

        log.info("orderId:{}", orderId);

        if (orderId == null) {
            throw new GlobalException(RespBeanEnum.ORDER_NOT_EXIST);
        }
        Order order = orderMapper.selectById(orderId);
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(order.getGoodsId());
        OrderDetailVo detailVo = new OrderDetailVo();
        detailVo.setOrder(order);
        detailVo.setGoodsVo(goodsVo);
        return detailVo;
    }

    /**
     * 获取秒杀地址
     *
     * @param user
     * @param goodsId
     * @return
     */
    @Override
    public String createPath(User user, Long goodsId) {
        String str = MD5Util.md5(UUIDUtil.uuid() + "123456");
        redisTemplate.opsForValue().set("seckillPath:" + user.getId() + ":" + goodsId, str, 60, TimeUnit.SECONDS);
        return str;
    }

    /**
     * 校验秒杀地址
     *
     * @param user
     * @param goodsId
     * @return
     */
    @Override
    public Boolean checkPath(User user, Long goodsId, String path) {
        if (user == null || goodsId < 0 || StringUtils.isEmpty(path)) {
            return false;
        }
        String redisPath = (String) redisTemplate.opsForValue().get("seckillPath:" + user.getId() + ":" + goodsId);
        log.info("path:{}", path);
        log.info("redisPath:{}", redisPath);
        return path.equals(redisPath);
    }

    /**
     * 验证码校验
     *
     * @param captcha
     * @return
     */
    @Override
    public Boolean checkCaptcha(User user, Long goodsId, String captcha) {
        if (user == null || goodsId<0 || StringUtils.isEmpty(captcha)) {
            return false;
        }
        String redisCaptcha = (String) redisTemplate.opsForValue().get("captcha:" + user.getId() + ":" + goodsId);
        return captcha.equals(redisCaptcha);
    }

}
