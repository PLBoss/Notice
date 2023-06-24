package com.nowcoder.community.util;

import com.nowcoder.community.entity.User;
import org.springframework.stereotype.Component;

@Component
public class HostHolder {
    private ThreadLocal<User> users=new ThreadLocal<>();
    //写入对象
    public void setUser(User user){
        users.set(user);
    }
    //获取对象
    public User getUser(){
        return users.get();
    }
    //清理对象
    public  void clear(){
        users.remove();
    }


}
