package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping(path = "/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    // 处理添加评论的请求时要注意,我们是希望能够在回复评论时候,直接将用户页面跳转回这个帖子页面的
    // 但是帖子的详情页面是有路径参数的
    // 所以最后重定向的地方就需要用到帖子的id,为了能够得到这个帖子的id,我们也配置为通过路径传入帖子的id
    // 同时页面传入的主要有,评论的内容,另外还有两个隐含的东西,提交的是哪种类型entity_type的评论,还有entity_id
    // 所以我们直接声明一个实体comment来接收,然后可能接收的值还不完整,我们补充一下即可
    @RequestMapping(path = "/add/{discussPostId}",method = RequestMethod.POST)
    public String addComment(Model model, @PathVariable("discussPostId")int discussPostId, Comment comment){

        // 实际为了传入userId,应该这么编写,(比如用户没登录的话让用户跳回首页)
        /*
        User user = hostHolder.getUser();
        if(user == null){
            return "/index";
        }
        comment.setUserId(user.getId());
        */
        // 但我们之后为完成一个统一处理的模块,所以这里先这么写
        // 但要记得没有统一模块处理之前这里是不安全的,如果用户没登录的话这里会报错
        // 之后会有统一异常的处理和统一权限的认证
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());

        // 额外添加这三个值之后就全了
        commentService.addComment(comment);

        return "redirect:/discuss/detail/" + discussPostId;
    }
}
