package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 不设置Controller路径是允许的,可以直接不用加这一级访问路径,直接使用方法的路径即可
@Controller
public class HomeController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/index",method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page,@RequestParam(name = "orderMode",defaultValue = "0") int orderMode){

        // 总行数
        // 首页 不以用户id为参数查询 所以参数填0
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index?orderMode=" + orderMode);
        // page还有当前页码,每页显示数据,html页面可以传进来,就无须服务器操心了

        List<DiscussPost> list = discussPostService.findDiscussPost(0,page.getOffset(),page.getLimit(),orderMode);
        // 这个集合里面是一个能够封装DiscussPost以及User对象的一个对象
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if(!list.isEmpty()){
            for(DiscussPost discussPost : list){
                Map<String,Object> map = new HashMap<>();
                map.put("post",discussPost);
                User user = userService.findUserById(discussPost.getUserId());
                map.put("user",user);

                // 中期扩展:添加帖子列表点赞数量显示
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,discussPost.getId());
                map.put("likeCount",likeCount);

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        // 最后返回模板的路径
        // 同时因为Thymeleaf的模板就是html文件,就是固定的一个文件,所以不需要写后缀,就只写文件名即可,只要心里知道下面这个路径的view指的是index.html即可
        model.addAttribute("orderMode",orderMode);
        return "/index";
    }

    // Controller发生异常以后,我统一处理,记录日志之后,我要干嘛呢?我要去到500那个页面,
    // 但是这个时候使我们人为处理的,我们需要手动的重定向过去,所以我们要把500请求页面的访问给配置一下
    @RequestMapping(path = "/error" ,method = RequestMethod.GET)
    public String getErrorPage(){
        return "/error/500";
    }

    // 权限不足时,普通请求跳转的错误页面
    @RequestMapping(path = "/denied", method = RequestMethod.GET)
    public String getDeniedPage() {
        return "/error/404";
    }

}
