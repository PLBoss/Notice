package com.nowcoder.community;


import com.nowcoder.community.service.alphaservice;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;


import java.util.Date;
import java.util.concurrent.*;


@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ThreadPoolTests {

    private static final Logger logger =LoggerFactory.getLogger(ThreadPoolTests.class);

    private ExecutorService executorService= Executors.newFixedThreadPool(5);

    private ScheduledExecutorService scheduledExecutorService= Executors.newScheduledThreadPool(5);

    public void sleep(long m){

        try {
            Thread.sleep(m);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void executor(){

        Runnable task=new Runnable() {
            @Override
            public void run() {
                logger.debug("hello-service");
            }
        };

        for (int i = 0; i < 10; i++) {

            executorService.submit(task);



        }

        sleep(10000);

    }

    @Test
    public void scheduledExecutor(){

        Runnable task=new Runnable() {
            @Override
            public void run() {
                logger.debug("hell0-service");
            }
        };
        //延迟1s执行，每次隔开1s执行
        scheduledExecutorService.scheduleAtFixedRate(task,1000,1000, TimeUnit.MILLISECONDS);

        sleep(200000);

    }

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    //spring自带的线程池

    @Test
    public  void threadPoolTaskExecutorTest(){
        Runnable task=new Runnable() {
            @Override
            public void run() {
                logger.debug("hell0-task-service");
            }
        };
        for (int i = 0; i < 10; i++) {
            threadPoolTaskExecutor.submit(task);
        }

        sleep(10000);

    }

    @Test
    public  void threadPoolTaskSchedulerTest(){
        Runnable task=new Runnable() {
            @Override
            public void run() {
                logger.debug("hell0-task-service");
            }
        };
        Date startTime=new Date(System.currentTimeMillis()+1000);
        threadPoolTaskScheduler.scheduleAtFixedRate(task,startTime,1000);

        sleep(30000);

    }

    @Autowired
    private alphaservice alphaservice;
    //简化注解开始普通多线程
    @Test
    public  void threadTest(){

        for (int i = 0; i < 10; i++) {

        }

        sleep(10000);

    }
    //简化版：定时任务
    @Test
    public  void scheduledThreadTest(){



        sleep(30000);

    }






}
