package com.ms.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ms.seckill.pojo.User;
import com.ms.seckill.vo.LoginVo;
import com.ms.seckill.vo.RespBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface IUserService extends IService<User> {

    RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response);

    User getUserByCookie(String userTicket,HttpServletRequest request,HttpServletResponse response);

    RespBean updatePassword(String userTicket,String password,HttpServletRequest request,HttpServletResponse response);
}
