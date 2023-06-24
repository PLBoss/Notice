package com.nowcoder.community.controller.advice;

import com.nowcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@ControllerAdvice(annotations = Controller.class)//扫描controller层的错误
public class ExceptionAdvice {
    //记录日志
    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {

        logger.error("服务器发生异常: " + e.getMessage());//记录错误日志
        for (StackTraceElement element : e.getStackTrace()) {
            logger.error(element.toString());
        }

        String xRequestedWith = request.getHeader("x-requested-with");//获取请求的方式
        //异步请求错误
        if ("XMLHttpRequest".equals(xRequestedWith)) {
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1, "服务器异常!"));
        } else {
            //普通请求错误
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }




}
