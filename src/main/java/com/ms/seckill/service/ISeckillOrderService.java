package com.ms.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ms.seckill.pojo.SeckillOrder;
import com.ms.seckill.pojo.User;

public interface ISeckillOrderService extends IService<SeckillOrder> {

    Long getResult(User user, Long goodsId);
}
