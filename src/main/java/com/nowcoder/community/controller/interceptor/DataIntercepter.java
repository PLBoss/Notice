package com.nowcoder.community.controller.interceptor;


import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DateService;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.spi.http.HttpHandler;

@Component
public class DataIntercepter implements HandlerInterceptor {

    /*拦截请求，统计数据*/

    @Autowired
    private DateService dateService;

    @Autowired
    private HostHolder hostHolder;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //统计uv
        String ip = request.getRemoteHost();
        dateService.recordUv(ip);



        //统计dau
        User user = hostHolder.getUser();
        if(user!=null){

            dateService.recordDau(user.getId());
        }




        return true;
    }
}
