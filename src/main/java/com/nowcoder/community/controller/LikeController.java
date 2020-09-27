package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.LikeService;
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
public class LikeController {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/like",method = RequestMethod.POST)
    @ResponseBody
    @LoginRequired
    public String like(int entityType,int entityId){
        User user = hostHolder.getUser();

        // 不用判断用户是否登陆,通过拦截器先要求登录
        // 之后会用Spring Security对项目重构,对拦截器进行重构,用Security统一管理这样的权限问题

        // 点赞
        likeService.like(user.getId(),entityType,entityId);
        // 数量
        long likeCount = likeService.findEntityLikeCount(entityType,entityId);
        // 状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(),entityType,entityId);

        Map<String,Object> map = new HashMap<>();
        map.put("likeCount",likeCount);
        map.put("likeStatus",likeStatus);

        return CommunityUtil.getJSONString(0,null,map);
    }
}
