package com.nowcoder.community;


import com.nowcoder.community.config.dataconfig;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.testDao;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.alphaservice;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import java.rmi.NoSuchObjectException;
import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
@ContextConfiguration(classes = NoticeApplication.class)
class CommunityApplicationTests implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;
    }
    @Test
    public void testApplication(){
        System.out.println(applicationContext);
        //根据类型获取，获取bean,并调用其中的方法
        testDao bean = applicationContext.getBean(testDao.class);
        System.out.println(bean.select());

        //根据名称获取bean,
        testDao dapImpl = applicationContext.getBean("dapImpl", testDao.class);
        System.out.println(dapImpl.select());


    }

    @Test
    public void testApplication1(){


        //根据名称获取bean,
        alphaservice dapImpl = applicationContext.getBean(alphaservice.class);
        System.out.println(dapImpl);
        dapImpl = applicationContext.getBean(alphaservice.class);
        System.out.println(dapImpl);


    }

    @Test
    public void testApplication2(){


        //根据名称获取bean,
//        dataconfig dapImpl = applicationContext.getBean(dataconfig.class);
//        System.out.println(dapImpl.simpleDateFormat().format(new Date()));
        SimpleDateFormat bean = applicationContext.getBean(SimpleDateFormat.class);
        System.out.println(bean.format(new Date()));

    }
    @Autowired
    @Qualifier("dapImpl")//获取指针的bean
    private testDao testDao;
    /*注入的方式获取bean*/
    @Test
    public  void testApplication3(){
        System.out.println(testDao);

    }

    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Test
    public  void testApplication4(){
        DiscussPost discussPost=new DiscussPost();

        discussPost.setUserId(89);
        discussPost.setCommentCount(3);
        discussPost.setContent("hello everyone");
        discussPost.setCreateTime(new Date());
        discussPost.setScore(3.33);
        discussPost.setStatus(0);
        int i = discussPostMapper.insertDiscussPostRows(discussPost);
        System.out.println(i);

    }
}
