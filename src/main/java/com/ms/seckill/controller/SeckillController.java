package com.ms.seckill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.ms.seckill.config.AccessLimit;
import com.ms.seckill.exception.GlobalException;
import com.ms.seckill.pojo.Order;
import com.ms.seckill.pojo.SeckKillMessage;
import com.ms.seckill.pojo.SeckillOrder;
import com.ms.seckill.pojo.User;
import com.ms.seckill.rabbitmq.MQSender;
import com.ms.seckill.service.IGoodsService;
import com.ms.seckill.service.IOrderService;
import com.ms.seckill.service.ISeckillOrderService;
import com.ms.seckill.utils.JSONUtil;
import com.ms.seckill.vo.GoodsVo;
import com.ms.seckill.vo.RespBean;
import com.ms.seckill.vo.RespBeanEnum;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/secKill")
@Slf4j
public class SeckillController implements InitializingBean {

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private ISeckillOrderService seckillOrderService;

    @Autowired
    private IOrderService orderService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MQSender mqSender;

    @Autowired
    private RedisScript<Long> script;

    private Map<Long, Boolean> emptyStockMap = new HashMap<>();


    /**
     * 秒杀
     * <p>
     * 优化前 ：  720
     * 优化后 ：  7385
     *
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(path = "/doSecKill/{path}", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSeckill(User user, Long goodsId, @PathVariable String path) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Boolean check = orderService.checkPath(user, goodsId, path);
        log.info("check:{}", check);
        if (!check) {
            return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
        }

        // 判断是否重复抢购
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if (seckillOrder != null) {
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }

        // 内存标记，减少Redis访问
        if (emptyStockMap.get(goodsId)) {
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }

        // 预减库存操作
        // Long stock = valueOperations.decrement("seckillGoods:" + goodsId); // 原子操作
        Long stock = (Long) redisTemplate.execute(script, Collections.singletonList("seckillGoods:" + goodsId), Collections.EMPTY_LIST);

        if (stock < 0) {
            emptyStockMap.put(goodsId, true);
            valueOperations.increment("seckillGoods:" + goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }

        // 异步下单
        SeckKillMessage seckKillMessage = new SeckKillMessage(user, goodsId);
        mqSender.sendSecKillMessage(JSONUtil.object2JsonStr(seckKillMessage));

        return RespBean.success(0);
    }


    /**
     * 生成秒杀地址
     *
     * @param user
     * @param goodsId
     * @return
     */
    @AccessLimit(second = 5,maxCount=5,needLogin=true)
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getPath(User user, Long goodsId,String captcha) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Boolean check = orderService.checkCaptcha(user, goodsId, captcha);
        // 校验验证码
        if (!check){
            return RespBean.error(RespBeanEnum.ERROR_CAPTCHA);
        }
        String str = orderService.createPath(user, goodsId);
        return RespBean.success(str);
    }

    /**
     * 获取秒杀记录
     *
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(path = "/result", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getResult(User user, Long goodsId) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Long orderId = seckillOrderService.getResult(user, goodsId);
        return RespBean.success(orderId);
    }

    /**
     * 生成验证码存入redis
     * @param user
     * @param goodsId
     * @param response
     */
    @RequestMapping(path = "/captcha", method = RequestMethod.GET)
    public void verifyCode(User user, Long goodsId, HttpServletResponse response) {
        if (user == null || goodsId < 0) {
            throw new GlobalException(RespBeanEnum.REQUEST_ILLEGAL);
        }
        // 设置请求头类型
        // 设置请求头为输出图片类型
        response.setContentType("image/gif");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 32, 3);
        redisTemplate.opsForValue().set("captcha:" + user.getId() + ":" + goodsId, captcha.text(), 300, TimeUnit.SECONDS);
        try {
            captcha.out(response.getOutputStream());
        } catch (IOException e) {
            log.error("验证码生成失效", e.getMessage());
        }
    }


    /**
     * 初始化，把商品存储数量加载进入redis
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> list = goodsService.findGoodsVo();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(goodsVo -> {
            redisTemplate.opsForValue().set("seckillGoods:" + goodsVo.getId(), goodsVo.getStockCount());
            emptyStockMap.put(goodsVo.getId(), false); // 内存标记
        });
    }


    /**
     * thymeleaf
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(path = "/doSecKill2")
    public String doSeckill2(Model model, User user, Long goodsId) {
        if (user == null) {
            return "login";
        }
        model.addAttribute("user", user);
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        //判断库存
        if (goodsVo.getStockCount() < 1) {
            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
            return "secKillFail";
        }
        //判断是否重复抢购
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if (seckillOrder != null) {
            model.addAttribute("errmsg", RespBeanEnum.REPEATE_ERROR.getMessage());
            return "secKillFail";
        }
        Order order = orderService.seckill(user, goodsVo);
        model.addAttribute("order", order);
        model.addAttribute("goods", goodsVo);
        return "orderDetail";
    }
}
