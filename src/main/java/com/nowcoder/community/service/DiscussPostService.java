package com.nowcoder.community.service;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    // 当前的方法实际上就是吧数据查出来,没有什么业务,有可能就有想法直接略过Service这层,直接在Controller内调用Mapper,但是这是不对的,为了将来业务考虑也不应该直接略过Service层

    public List<DiscussPost> findDiscussPost(int userId, int offset, int limit){
        return discussPostMapper.selectDiscussPosts(userId,offset,limit);
    }

    public int findDiscussPostRows(int userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    // 还有一个问题是我们查询到的这个结果,他有一个外键userId,但是在页面显示的时候要显示的是user的名称,而不是userId,
    // 有两种解决办法,1是写sql都时候,关联查询用户,把用户的数据也一起查到,2是单独的查到user,然后把查到的user对象和discussPost一起返回
    // 建议选择第二种,虽然现在麻烦一些,但是之后用redis缓存数据的时候就会快很多


}
