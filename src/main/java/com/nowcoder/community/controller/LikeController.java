package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements CommunityConstant {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(path = "/like",method = RequestMethod.POST)
    @ResponseBody
    @LoginRequired
    public String like(int entityType,int entityId,int entityUserId,int postId){
        User user = hostHolder.getUser();

        // 不用判断用户是否登陆,通过拦截器先要求登录
        // 之后会用Spring Security对项目重构,对拦截器进行重构,用Security统一管理这样的权限问题

        // 点赞
        likeService.like(user.getId(),entityType,entityId,entityUserId);
        // 数量
        long likeCount = likeService.findEntityLikeCount(entityType,entityId);
        // 状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(),entityType,entityId);

        Map<String,Object> map = new HashMap<>();
        map.put("likeCount",likeCount);
        map.put("likeStatus",likeStatus);

        // 5.11
        // 点赞这个业务是一个有双重能力的,点一下是赞,再点一下是取消赞
        // 我们只在点赞通知即可,所以要判断likeStatus==1的情况下才触发事件
        // 触发点赞事件
        if(likeStatus == 1){
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    // 某某人赞了你的帖子/评论的帖子,点击查看,那么一点击也是去帖子的详情页面,那我们也需要content字段有条件获取帖子id
                    // 但是在这里是得不到这个帖子id的,当然我们可以通过给谁点赞去查,但是这样比较麻烦,我重构一下方法,要求方法多接受一个参数
                    // 因为我们是在帖子详情页面点赞的,那个时候是很容易得到帖子id的
                    .setData("postId",postId);
            eventProducer.fireEvent(event);
        }

        return CommunityUtil.getJSONString(0,null,map);
    }
}
