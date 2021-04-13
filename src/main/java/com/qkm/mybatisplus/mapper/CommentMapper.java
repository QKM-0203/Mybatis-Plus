package com.qkm.mybatisplus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qkm.mybatisplus.ZD.Comment;
import org.apache.ibatis.annotations.Param;

/**
* @Entity com.qkm.mybatisplus.ZD.Comment
*/
public interface CommentMapper extends BaseMapper<Comment> {
    int deleteByCommentAndBlogcreatatandname(@Param("comment") String comment, @Param("blogcreatatandname") String blogcreatatandname);
    int insertSelective(Comment comment);
}
