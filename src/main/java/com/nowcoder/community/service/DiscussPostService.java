package com.nowcoder.community.service;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.DiscussPostAndUserName;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    // 当前的方法实际上就是吧数据查出来,没有什么业务,有可能就有想法直接略过Service这层,直接在Controller内调用Mapper,但是这是不对的,为了将来业务考虑也不应该直接略过Service层

    public List<DiscussPost> findDiscussPost(int userId, int offset, int limit,int orderMode){
        return discussPostMapper.selectDiscussPosts(userId,offset,limit,orderMode);
    }

    public int findDiscussPostRows(int userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    // 还有一个问题是我们查询到的这个结果,他有一个外键userId,但是在页面显示的时候要显示的是user的名称,而不是userId,
    // 有两种解决办法,1是写sql都时候,关联查询用户,把用户的数据也一起查到,2是单独的查到user,然后把查到的user对象和discussPost一起返回
    // 建议选择第二种,虽然现在麻烦一些,但是之后用redis缓存数据的时候就会快很多

    // 添加帖子业务
    public int addDiscussPost(DiscussPost discussPost){
        if(discussPost == null){
            throw new IllegalArgumentException("参数不能为空!");
        }
        // title和content是需要做敏感词过滤的,同时最好把<script></script>这种标签也给去掉,我们只希望浏览器认为这是普通的文字,而不希望认为这是html标签
        // 转义html标记,利用Spring MVC自带的一个工具即可
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));

        // 过滤敏感词
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));

        return discussPostMapper.insertDiscussPost(discussPost);
    }

    // 查询帖子
    public DiscussPost findDiscussPostById(int id){
        return discussPostMapper.selectDiscussPostById(id);
    }

    // 查询帖子另一种方法(self)
    public DiscussPostAndUserName findDiscussPostAndUserNameById(int id){
        return discussPostMapper.selectDiscussPostAndUserNameById(id);
    }

    // 在帖子的业务组件加一个更新comment_count评论数的方法
    // 因为毕竟comment_count是discuss_post表的列
    // 然后在CommentService编写评论业务的时候,加上这个业务组件
    // 也就是说Service不止可以依赖自己的Mapper,也可以依赖其他的Service
    public int updateCommentCount(int id,int commentCount){
        return discussPostMapper.updateCommentCount(id,commentCount);
    }

    public int updateType(int id, int type) {
        return discussPostMapper.updateType(id, type);
    }

    public int updateStatus(int id, int status) {
        return discussPostMapper.updateStatus(id, status);
    }

    public int updateScore(int id, double score) {
        return discussPostMapper.updateScore(id, score);
    }


}
