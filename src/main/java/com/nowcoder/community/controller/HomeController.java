package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 不设置Controller路径是允许的,可以直接不用加这一级访问路径,直接使用方法的路径即可
@Controller
public class HomeController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @RequestMapping(path = "/index",method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page){

        // 总行数
        // 首页 不以用户id为参数查询 所以参数填0
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");
        // page还有当前页码,每页显示数据,html页面可以传进来,就无须服务器操心了

        List<DiscussPost> list = discussPostService.findDiscussPost(0,page.getOffset(),page.getLimit());
        // 这个集合里面是一个能够封装DiscussPost以及User对象的一个对象
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if(!list.isEmpty()){
            for(DiscussPost discussPost : list){
                Map<String,Object> map = new HashMap<>();
                map.put("post",discussPost);
                User user = userService.findUserById(discussPost.getUserId());
                map.put("user",user);
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        // 最后返回模板的路径
        // 同时因为Thymeleaf的模板就是html文件,就是固定的一个文件,所以不需要写后缀,就只写文件名即可,只要心里知道下面这个路径的view指的是index.html即可
        return "/index";
    }

}
