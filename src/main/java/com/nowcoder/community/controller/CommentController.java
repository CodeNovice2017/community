package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.util.CommunityConstant;
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
public class CommentController implements CommunityConstant {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;

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

        // 5.11
        // 添加评论以后就要通知了
        // 触发了评论事件
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId",discussPostId);
        // 上面的event还有一个entityUserId没有拼,
        // 因为我评论的可能是帖子,也可能是评论的评论,那么entityUserId就要查,如果是帖子那就要查帖子表的userId,如果是评论那就要在comment表的userId
        if(comment.getEntityType() == ENTITY_TYPE_POST){
            // 触发的事件是评论事件,所以entityType=2,但是查询评论这个业务时,帖子这个实体是ENTITY_TYPE_POST,这两个entityType是两个概念,
            // 对帖子的回复,那么UserId就是当前用户的id,entityUserId就是用户评论的帖子的id,
            // 事件触发后,用户的系统通知列表应该是查询toId=entityUserId来显示消费者放入message表的消息的
            DiscussPost target = discussPostService.findDiscussPostById(discussPostId);
            event.setEntityUserId(target.getUserId());
        }else if(comment.getEntityType() == ENTITY_TYPE_COMMENT){
            // 触发的事件是评论,具体是评论的评论,这条评论的评论应该是通过comment.getEntityId()来找到是对哪条帖子评论的评论
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        eventProducer.fireEvent(event);

        // 还要注意的就是发布评论的时候,发布评论,那么帖子的评论数量就变了,相当于改了帖子,那这个时候还需要触发一次这个事件,覆盖掉ES内之前的帖子的数据
        // 先判断,是对帖子的评论再去触发发帖事件,因为对评论的评论是不影响帖子的评论数的
        if(comment.getEntityType() == ENTITY_TYPE_POST){
            event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
            eventProducer.fireEvent(event);
        }


        return "redirect:/discuss/detail/" + discussPostId;
    }
}
