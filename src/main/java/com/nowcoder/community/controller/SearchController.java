package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.service.ElasticsearchService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private ElasticsearchService elasticsearchService;

    // 搜到了帖子还要展现出帖子的作者,还要展现出帖子点赞的数量
    @Autowired
    private UserService userService;
    @Autowired
    private LikeService likeService;

    // 查询所以用GET
    @RequestMapping(path = "/search",method = RequestMethod.GET)
    // search?keyword=xxx
    // keyword因为当前是GET请求,get请求的话,keyword就不能用请求体来传,get请求要么就是路径后面带?传参,要么就是路径中某一级来传
    public String search(String keyword, Page page, Model model){
        // 搜索帖子
        // 我们封装的Page的current是从1开始的,但是ES的current是从0开始的,所以要减1
        // 因为Page对象和我们自己的Page对象冲突了,所以使用Spring的Page对象的话就要带上包名,以免歧义org.springframework.data.domain.Page
        org.springframework.data.domain.Page<DiscussPost> searchResult = elasticsearchService.searchDiscussPost(keyword, page.getCurrent()-1, page.getLimit());
        // 这里得到的只是一个DiscussPost实体,这里面包含的userId,我们需要更多的User的数据,同时我们也需要查询这个帖子有多少点赞数量
        // 所以需要将这个数据再聚合一次
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if(searchResult != null){
            for (DiscussPost post:
                 searchResult) {
                // 每次遍历,我要新建一个map,封装聚合的数据
                Map<String,Object> map = new HashMap<>();
                // 帖子
                map.put("post",post);
                // 坐这儿
                map.put("user",userService.findUserById(post.getUserId()));
                // 点赞的数量
                map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        // 另外,当返回到页面的时候,把刚才搜索的关键字也给用户返回去
        model.addAttribute("keyword",keyword);

        // 分页信息
        page.setPath("/search?keyword=" + keyword);
        page.setRows(searchResult == null ? 0 : (int)searchResult.getTotalElements());
        return "/site/search";
    }
}
