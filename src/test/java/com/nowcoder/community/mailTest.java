package com.nowcoder.community;



import com.nowcoder.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class mailTest {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testTextMail()   {
        mailClient.sendMail("3309327228@qq.com", "TEST", "Welcome.");
    }

    @Test
    public void testHtmlMail() {
        Context context = new Context();
//       /*给模板赋值*/
        context.setVariable("username", "sunday");
//              指定模板的路径
        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);
//            发送文件
        mailClient.sendMail("3309327228@qq.com", "HTML", content);
    }



}
