package com.ms.seckill.vo;

import com.ms.seckill.pojo.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能描述:
 *
 * @author:Eddie
 * @date:2022/1/13
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailVo {

    private Order order;
    private GoodsVo goodsVo;

}
