package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

@Controller
@RequestMapping(path = "/discuss")
public class DiscussPostController {

    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    // 页面上只传递过来标题和内容,Controller就是处理请求的,所以以后见到Controller方法的参数就要想到这都是接收到请求中的参数
    // 表中剩下需要的字段id自动生成的 userId是当前的用户(HostHolder) createTime要自己生成,score,评论数等都没有,type和status没有管理员设置管理之前都是普通帖子
    public String addDiscussPost(String title,String content){

        User user = hostHolder.getUser();
        if(user == null){
            return CommunityUtil.getJSONString(403,"你还没有登录!");
        }

        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(user.getId());
        discussPost.setTitle(title);
        discussPost.setContent(content);
        // type和status默认就是0,不用非得设置也可以
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);

        // 如果说执行到这里,那么就代表发布成功,那么有人会问难道上面的代码就确保成功不会出错吗,将来我们会统一处理这些问题
        // 报错的情况将来统一处理
        return CommunityUtil.getJSONString(0,"发布成功!");
    }

    @LoginRequired
    @RequestMapping(path = "/detail/{discussPostId}",method = RequestMethod.GET)
    public String getDiscussPost(Model model,@PathVariable("discussPostId") int discussPostId){
        // 查询帖子
        DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post",discussPost);
        User user = userService.findUserById(discussPost.getUserId());
        model.addAttribute("user",user);
        return "/site/discuss-detail";
    }

}
