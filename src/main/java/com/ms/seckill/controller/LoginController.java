package com.ms.seckill.controller;

import com.ms.seckill.service.IUserService;
import com.ms.seckill.vo.LoginVo;
import com.ms.seckill.vo.RespBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@RequestMapping("/login")
@Slf4j
public class LoginController {

    @Autowired
    private IUserService userService;

    @RequestMapping("/toLogin")
    public String toLogin(){
        return "login";
    }


    /**
     * 优化前 ： QPS 云mysql    windows 112
     *         QPS 云mysql   Linux  65
     *
     *
     * @param loginVo
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/doLogin")
    @ResponseBody
    public RespBean doLogin(@Valid LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
        log.info("{}", loginVo);
        return userService.doLogin(loginVo, request, response);
    }
}
