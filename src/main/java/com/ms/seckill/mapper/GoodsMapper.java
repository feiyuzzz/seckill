package com.ms.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ms.seckill.pojo.Goods;
import com.ms.seckill.vo.GoodsVo;

import java.util.List;

public interface GoodsMapper extends BaseMapper<Goods> {
    List<GoodsVo> findGoodsVo();

    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
