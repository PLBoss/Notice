package com.nowcoder.community.service;

import com.nowcoder.community.dao.testDao;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
@Service
//@Scope("prototype")
public class alphaservice {

    private static  final Logger logger= LoggerFactory.getLogger(alphaservice.class);

    //构造实例化
    public alphaservice(){
        System.out.println("实例化alphaService");
    }

    //初始示例化
    @PostConstruct
    public void init(){
        System.out.println("初始化alphaService");
    }
    //销毁对象
    @PreDestroy
    public void destory(){
        System.out.println("销毁alphaService");
    }

    @Autowired
    private testDao testDao;
    /*调用dao*/
    public String find(){

        return  testDao.select();
    }

//    @Async
//    public  void execute1(){
//        logger.debug("hello-execute1");
//    }
//
//    //initialDelay多久时间调一回，fixDelay 执行的时间间隔
//    @Scheduled(initialDelay = 10000,fixedDelay = 1000)
//    public  void execute2(){
//        logger.debug("hello-scheld");
//    }


}
