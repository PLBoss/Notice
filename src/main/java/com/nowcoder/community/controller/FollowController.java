package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;

import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.service.followService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {

    @Autowired
    private followService followService;

    @Autowired
    private HostHolder hostHolder;
    
    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventproducer;

    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();

        followService.follow(user.getId(), entityType, entityId);

        //触发关注事件

        Event event=new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);//只能关注人，所以实体Id,就是用户id

        eventproducer.fireEvent(event);

        return CommunityUtil.getJSONString(0, "已关注!");
    }

    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType, int entityId) {
        User user = hostHolder.getUser();

        followService.unfollow(user.getId(), entityType, entityId);

        return CommunityUtil.getJSONString(0, "已取消关注!");
    }
    
    //查看关注列表
    @RequestMapping("/followees/{userId}")
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.findUserById(userId);

        if (user==null) {
            throw new RuntimeException("用户不存在了");
        }

        model.addAttribute("user",user);

        page.setLimit(5);
        page.setPath("/followees/"+userId);
        page.setRows((int) followService.findFolloweeCount(userId,ENTITY_TYPE_USER));


        List<Map<String, Object>> followees = followService.findFollowees(userId, page.getOffset(), page.getLimit());

        if (followees!=null) {

            for (Map<String, Object> map : followees) {
                 User u = (User) map.get("user");

                 map.put("hasFollowed",hasFollowed(u.getId()));


            }
        }
        model.addAttribute("users",followees);
        return "/site/followee";
    }


    //查看粉丝列表
    @RequestMapping("/followers/{userId}")
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model){
        User user = userService.findUserById(userId);

        if (user==null) {
            throw new RuntimeException("用户不存在了");
        }

        model.addAttribute("user",user);

        page.setLimit(5);
        page.setPath("/followers/"+userId);
        page.setRows((int) followService.findFolloweeCount(userId,ENTITY_TYPE_USER));


        List<Map<String, Object>> followers = followService.findFollowees(userId, page.getOffset(), page.getLimit());

        if (followers!=null) {

            for (Map<String, Object> map : followers) {
                User u = (User) map.get("user");

                map.put("hasFollowed",hasFollowed(u.getId()));


            }
        }
        model.addAttribute("users",followers);
        return "/site/follower";
    }


    public boolean hasFollowed(int  userId){

        if(hostHolder.getUser()==null){
            return  false;
        }

        return followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
    }

}
