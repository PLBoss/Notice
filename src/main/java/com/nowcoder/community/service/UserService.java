package com.nowcoder.community.service;

import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import sun.security.krb5.internal.Ticket;


import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {
    @Autowired//外键查询userid的用户信息
    private UserMapper userMapper;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;

    public User findUserById(int userId){

//        return userMapper.selectById(userId);
        User user = getCache(userId);

        if (user==null) {
            user=initCache(userId);
        }


        return  user;
    }

    public Map<String,Object> register(User user) {

        Map<String,Object> map=new HashMap<>();
        //空值的处理

        if (user == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }


        //判断账号是否为空


        //验证账号是否已经存在
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已存在!");
            return map;
        }

        // 验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册!");
            return map;
        }


        //注册
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));//生成随机掩码
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));//带上掩码进行加密
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());//生成随机激活码
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);



        //发送激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/community/activation/101/code
        //拼接激活地址
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;

    }


    /*激活邮件*/
    public  int activation(int userId,String code){
//        User user = userMapper.selectById(userId);
        User user = getCache(userId);
        if (user==null) {
             user = initCache(userId);
        }




        if(user.getStatus()==1){
            return ACTIVATION_REPEAT;//已经激活
        }else if(user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId,1);//更新用户状态
            clearCache(userId);
            return ACTIVATION_SUCCESS;//激活成功
        }else{
            return ACTIVATION_FAILURE;//激活失败
        }
    }

    public Map<String, Object> login(String username, String password, long expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        // 验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在!");
            return map;
        }

        // 验证状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活!");
            return map;
        }

        // 验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确!");
            return map;
        }

        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
//        loginTicketMapper.insertLoginTicket(loginTicket);

        //使用redis进行存储
        String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(ticketKey,loginTicket);


        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket){

        /*loginTicketMapper.updateStatus(ticket,1);*/
        //将redis查询出登陆凭证，查询出凭证对象，然后修改凭证的对象，然后再将凭证存进redis中
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);

         LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);

         loginTicket.setStatus(1);

         redisTemplate.opsForValue().set(ticketKey,loginTicket);

    }

    //查询凭证
    public LoginTicket findLoginTicket(String ticket){
//        return loginT icketMapper.selectByTicket(ticket);

        String ticketKey = RedisKeyUtil.getTicketKey(ticket);

        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);

        return loginTicket;

    }
    //更新用户头像路径
    public  int updateHeader(int userId,String headerUrl){

//        return userMapper.updateHeader(userId,headerUrl);

        int row = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return row;

    }

    //更新用户密码

    // 重置密码

    public Map<String,Object> updatePassword(String ticket,int id,String oldPassword,String newPassword){

        Map<String,Object> map=new HashMap<>();



        User user = userMapper.selectById(id);

        // 空值处理
        if (StringUtils.isBlank(oldPassword)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(newPassword)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }



        // 验证密码
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if (!user.getPassword().equals(oldPassword)) {
            map.put("passwordMsg", "密码不正确!");
            return map;
        }

        //更新密码
        newPassword=CommunityUtil.md5(newPassword+user.getSalt());

        userMapper.updatePassword(id,newPassword);

        //更新凭证的状态

//        loginTicketMapper.updateStatus(ticket,1);
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey,loginTicket);

        return map;

    }
    //根据用户名字查询用户
    public User findUserByName(String userName){
        return userMapper.selectByName(userName);
    }


    //1.优先从缓存中取值
    private User getCache(int userId){
        String userKey = RedisKeyUtil.getUserKey(userId);

        return (User) redisTemplate.opsForValue().get(userKey);

    }
    //2.取不到时初始化缓存数据
    public User initCache(int userId){
        User user = userMapper.selectById(userId);
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey,user,3600, TimeUnit.SECONDS);
        return  user;


    }

    //3.数据变更时清除缓存
    private  void clearCache(int userId){
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);

    }

    //得到用户的权限

    public Collection<? extends GrantedAuthority> getAuthorities(int userId){

        User user= this.findUserById(userId);

        List<GrantedAuthority> list=new ArrayList<>();

        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {

                switch (user.getType()){
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }

            }
        });

        return  list;


    }



   
}
