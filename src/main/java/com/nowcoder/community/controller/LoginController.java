package com.nowcoder.community.controller;


import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${server.servlet.context-path}")
    private String contextPath;



    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }
    //增加登陆的路由
    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }



    //   注册
    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            //没有错误，才能发送邮件
            model.addAttribute("msg", "注册成功,我们已经向您的邮箱发送了一封激活邮件,请尽快激活!");
            model.addAttribute("target", "/index");
            return "/site/operate-result";//成功后跳转
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";//错误提示页面
        }
    }
    //处理激活
    @RequestMapping(path="/activation/{userId}/{code}")
    public String activation(Model model, @PathVariable("userId") int userId,@PathVariable("code") String code){
        int activation = userService.activation(userId, code);
        if(activation==ACTIVATION_SUCCESS){
            //给teyleaf传值

            model.addAttribute("msg", "激活成功!");
            model.addAttribute("target", "/login");
        }else if(activation==ACTIVATION_REPEAT){
            model.addAttribute("msg", "该账户已激活");
            model.addAttribute("target", "/index");
        }else{
            model.addAttribute("msg", "激活失败！");
            model.addAttribute("target", "/index");

        }


        return "/site/operate-result";
    }

    //生成验证码
    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response /*HttpSession session*/) {
        // 生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        //生成验证码的标识主机,将验证码的标识存在cookie中，然后通过解析cookies即可
        String kapthaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kapthaOwner", kapthaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);

        //将生成的验证码存入redis中
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(kapthaOwner);
        redisTemplate.opsForValue().set( kaptchaKey,text, 60,TimeUnit.SECONDS);


        // 将图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("响应验证码失败:" + e.getMessage());
        }
    }

    //校验登陆,取出之前存入session的验证码，并将这个登陆信息写入cookies中
    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public  String login(String username,String password,String code,boolean rememberMe,
                         Model model /*HttpSession session*/,HttpServletResponse response
                         ,@CookieValue("kapthaOwner") String kapthaOwner){
        //String  kaptcha = (String) session.getAttribute("kaptcha");
        String kaptcha=null;

        if (StringUtils.isNotBlank(kapthaOwner)) {

            String kaptchaKey = RedisKeyUtil.getKaptchaKey(kapthaOwner);

             kaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);


        }


        //首先校验验证码是否正确，成功了再校验账号密码
        if (StringUtils.isBlank(kaptcha)||StringUtils.isBlank(code) ||!kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg","验证码错误");

            return "/site/login";
        }
        //校验账号密码，这里不是直接采用对比账号密码实现的，而是通过之前登陆过程中的service产生的登陆凭证
        //实现的，只要包含登陆凭证，就代表能成功登陆
        int expiredSeconds=rememberMe?REMEMBER_EXPIRED_SECONDS:DEFAULT_EXPIRED_SECONDS;

        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        
        if(map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);

            return "redirect:/index";


        }else{

            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
            
        }


    }

    //退出登陆
    @RequestMapping(path = "/logout",method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);

        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }


    //修改密码逻辑：原密码输入不正确，不能进行修改，还有进行提示，
    // 输入新密码成功后，还要将用户凭证状态设置1，然后跳转到登陆界面
    @RequestMapping(path = "/updatePassword", method = RequestMethod.POST)
    public String updatePassword(@CookieValue("ticket") String ticket, String oldPassword, String newPassword, Model model){

        LoginTicket loginTicket = userService.findLoginTicket(ticket);
        int id =loginTicket.getUserId();
        System.out.println(id);

        Map<String, Object> map = userService.updatePassword(ticket,id, oldPassword, newPassword);



        if (map.containsKey("passwordMsg")) {

            model.addAttribute("passwordMsg",map.get("passwordMsg"));

        }else {

            return "redirect:/login";



        }

         return "/site/setting";



    }



}
