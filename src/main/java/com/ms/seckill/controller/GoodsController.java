package com.ms.seckill.controller;

import com.ms.seckill.pojo.User;
import com.ms.seckill.service.IGoodsService;
import com.ms.seckill.vo.DetailVo;
import com.ms.seckill.vo.GoodsVo;
import com.ms.seckill.vo.RespBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/goods")
@Slf4j
public class GoodsController {


    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;

    /**
     * Windows优化前QPS ：云数据库 264   添加页面缓存  320
     * Linux优化前QPS :  云数据库 176    添加页面缓存
     *
     * @param model
     * @param user
     * @return
     */
    @RequestMapping(path = "/toList", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toList(Model model, User user, HttpServletRequest request, HttpServletResponse response) {

        //Redis 中获取页面，如果不为空，直接返回页面
        ValueOperations operations = redisTemplate.opsForValue();
        String html = (String) operations.get("goodsList");
        if (!StringUtils.isEmpty(html)) {  //
            return html;
        }
        model.addAttribute("user", user);
        model.addAttribute("goodsList", goodsService.findGoodsVo());
        // 如果为空则手动渲染，存入Redis返回
        WebContext webContext = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsList", webContext);
        if (!StringUtils.isEmpty(html)) {
            operations.set("goodsList", html, 60, TimeUnit.SECONDS);
        }
        return html;
    }

    @RequestMapping(path = "/toDetail/{goodsId}", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toDetail2(Model model, User user, @PathVariable Long goodsId, HttpServletRequest request, HttpServletResponse response) {
        ValueOperations operations = redisTemplate.opsForValue();
        String html = (String) operations.get("goodsDetails:" + goodsId);
        // 从Redis获取缓冲页面
        if (!StringUtils.isEmpty(html)) {
            return html;
        }

        model.addAttribute("user", user);
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        Date startDate = goodsVo.getStartDate();
        Date endDate = goodsVo.getEndDate();
        Date nowDate = new Date();

        // 秒杀状态
        int seckillStatus = 0;
        // 秒杀倒计时
        int remainSeconds = 0;
        if (nowDate.before(startDate)) {
            //秒杀还未开始
            remainSeconds = (int) (((startDate.getTime() - nowDate.getTime()) / 1000));
        } else if (nowDate.after(endDate)) {
            //秒杀已结束
            seckillStatus = 2;
            remainSeconds = -1;
        } else {
            seckillStatus = 1;
            remainSeconds = 0;
        }
        model.addAttribute("remainSeconds", remainSeconds);
        model.addAttribute("secKillStatus", seckillStatus);
        model.addAttribute("goods", goodsVo);

        WebContext webContext = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsDetail", webContext);
        if (!StringUtils.isEmpty(html)) {
            operations.set("goodsDetails:" + goodsId, html, 60, TimeUnit.SECONDS);
        }
        return html;
    }


    @RequestMapping(value = "/detail/{goodsId}")
    @ResponseBody
    public RespBean toDetail(User user, @PathVariable Long goodsId) {
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        Date startDate = goodsVo.getStartDate();
        Date endDate = goodsVo.getEndDate();
        Date nowDate = new Date();
        // 秒杀状态
        int seckillStatus = 0;
        // 秒杀倒计时
        int remainSeconds = 0;
        if (nowDate.before(startDate)) {
            //秒杀还未开始
            remainSeconds = (int) (((startDate.getTime() - nowDate.getTime()) / 1000));
        } else if (nowDate.after(endDate)) {
            //秒杀已结束
            seckillStatus = 2;
            remainSeconds = -1;
        } else {
            seckillStatus = 1;
            remainSeconds = 0;
        }
        DetailVo detailVo = new DetailVo();
        detailVo.setUser(user);
        detailVo.setGoodsVo(goodsVo);
        detailVo.setSecKillStatus(seckillStatus);
        detailVo.setRemainSeconds(remainSeconds);
        return RespBean.success(detailVo);
    }

}
