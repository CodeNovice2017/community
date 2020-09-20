package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    // 根据entityType来查询是帖子的评论还是评论的评论还是课程的评论呢(实体的两个条件entityType,entityId)
    // offset,limit分页的条件
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    // 查询数据的条目数
    int selectCountByEntity(int entityType, int entityId);

    int insertComment(Comment comment);
}
