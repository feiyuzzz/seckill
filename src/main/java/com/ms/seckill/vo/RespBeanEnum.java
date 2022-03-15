package com.ms.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor
public enum RespBeanEnum {

    // 通用
    SUCCESS(200,"成功"),
    ERROR(500,"服务端异常"),
    // 登陆模块5002xx
    LOGIN_ERROR(500211,"用户名或者密码错误"),
    MOBILE_ERROR(500212,"手机号码格式不正确"),
    BIND_ERROR(500213,"参数校验异常"),
    MOBILE_NOT_EXIT(500214,"手机号码不存在"),
    PASSWORD_UPDATE_FAIL(500215,"更新密码失败"),
    SESSION_ERROR(500216,"密码更新失败"),
    // 秒杀模块5005xx
    EMPTY_STOCK(500500,"库存不足"),
    REPEATE_ERROR(500501,"该商品每人限购一件"),
    REQUEST_ILLEGAL(500502,"请求非法，请重新尝试"),
    ERROR_CAPTCHA(500503,"验证码错误"),
    ACCESS_LIMIT_REAHCED(500504,"请求过于频繁，请稍后"),
    // 订单模块5003xx
    ORDER_NOT_EXIST(500300,"订单不存在");


    private final Integer code;
    private final String message;

}
