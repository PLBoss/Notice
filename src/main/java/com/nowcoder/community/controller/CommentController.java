package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.event.EventProducer;

import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private EventProducer eventproducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {



        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());

        commentService.addComment(comment);

        //触发消息队列
        Event event=new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityId(comment.getEntityId())
                .setEntityType(comment.getEntityType())
                .setData("postId",discussPostId);




        //将评论的实体的用户查出来进去
        if(comment.getEntityType()==ENTITY_TYPE_POST){
            //查贴子的用户id
            DiscussPost discussPost = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(discussPost.getUserId());


        }else if(comment.getEntityType()==ENTITY_TYPE_COMMENT){
            //查评论的id
            Comment target = commentService.findCommentsById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());




        }

        eventproducer.fireEvent(event);

        //触发帖子评论事件
        if(comment.getEntityType()==ENTITY_TYPE_POST){

            event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
            eventproducer.fireEvent(event);

            //记录评论，然后计算分
            String postScoreKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(postScoreKey,discussPostId);

        }


        return "redirect:/discuss/detail/" + discussPostId;
    }



}
