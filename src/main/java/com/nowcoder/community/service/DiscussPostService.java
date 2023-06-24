package com.nowcoder.community.service;

import com.nowcoder.community.dao.CommentMapper;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService implements CommunityConstant {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<DiscussPost> findDiscussPosts(int userId,int offset,int limit,int orderModel){
        return discussPostMapper.selectDiscussPosts( userId, offset, limit,orderModel);
    }

    public int findDiscussPostRows(int userId){
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public int addDiscussPost(DiscussPost post){
        if(post==null){
            throw new IllegalArgumentException("参数不能为空");
        }

        //转移输入的特殊符号
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));

        //g过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));

        return  discussPostMapper.insertDiscussPostRows(post);
    }

    public DiscussPost findDiscussPostById(int discussPostId) {

        return discussPostMapper.selectDiscussPostById(discussPostId);
    }


    public int updateCommentCount(int id, int commentCount) {
        return discussPostMapper.updateCommentCount(id, commentCount);
    }


    public int updateType(int type,int id){
        return discussPostMapper.updateType(id,type);
    }

    public int updateStatus(int status,int id){
        return discussPostMapper.updateStatus(id,status);
    }

    public int updateScore(int id,double score){
        return discussPostMapper.updateScore(id,score);
    }






}
