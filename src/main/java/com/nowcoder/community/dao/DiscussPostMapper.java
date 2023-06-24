package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    //查询帖子，并按照分页返回
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit,int orderModel);
    //@Param注解用于给参数取别名
    //如果只有一个参数，并且使用if标签，则必须使用别名
    int selectDiscussPostRows(@Param("userId") int userId);

    int insertDiscussPostRows(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int discussPostId);
    int updateCommentCount(int id, int commentCount);

    int updateType(int id,int type);

    int updateStatus(int id,int status);

    int updateScore(int id,double score);
}
