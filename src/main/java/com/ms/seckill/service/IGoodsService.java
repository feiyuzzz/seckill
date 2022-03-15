package com.ms.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ms.seckill.pojo.Goods;
import com.ms.seckill.vo.GoodsVo;

import java.util.List;


public interface IGoodsService extends IService<Goods> {

    List<GoodsVo> findGoodsVo();

    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
