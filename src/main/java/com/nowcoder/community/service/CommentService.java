package com.nowcoder.community.service;

import com.nowcoder.community.dao.CommentMapper;
import com.nowcoder.community.entity.Comment;
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
public class CommentService implements CommunityConstant {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    // 增加评论的业务
    // 这个业务方法包含两个DML数据库操作操作
    // 所以希望对它进行事务管理,保证这两次操作在一个事务范围之内,要么全成功,要么全失败
    // 当前整个方法在一个事务范围之内,并不是局部的,所以我们使用声明式事务编程方法
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public int addComment(Comment comment){
        // 首先要增加评论,那么对页面传入的这个实体,我们需要对内容进行一个过滤,包括Html标签过滤,包括敏感词过滤
        if(comment == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        // Html标签过滤
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        // 敏感词过滤
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = commentMapper.insertComment(comment);

        // 更新评论数量
        // 评论的目标是不一样的,我可以评论给评论,可以评论给帖子,可以评论给其他内容等
        // 而只有评论给帖子的时候,才需要更新帖子的评论数量
        if(comment.getEntityType() == ENTITY_TYPE_POST){
            int count = commentMapper.selectCountByEntity(comment.getEntityType(),comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(),count);
        }
        return rows;
    }

    public Comment findCommentById(int id){
        return commentMapper.selectCommentById(id);
    }

}
