package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    @LoginRequired
    // 因为是提交数据的过程,所以应该是POST请求
    @RequestMapping(path = "/follow",method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType,int entityId){
        User user = hostHolder.getUser();
        followService.follow(user.getId(),entityType,entityId);

        // 5.11
        // 触发关注事件
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(hostHolder.getUser().getId())
                // 目前关注是只关注了用户的,所以entityType只为3的
                .setEntityType(entityType)
                .setEntityId(entityId)
                // 目前只能关注人,所以entityUesrId也只能是entityId,因为entityId在关注业务中目前就是userId
                .setEntityUserId(entityId);
                // 关注是用户XX关注了你,通过这条消息点击查看不是连接某个帖子,而是连接到关注你的这个人的主页上去,
                // 而评论/点赞都需要转到帖子详情页去,所以需要postId,而此处不用
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0,"已关注");
    }

    @LoginRequired
    @RequestMapping(path = "/unfollow",method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType,int entityId){
        User user = hostHolder.getUser();
        followService.unfollow(user.getId(),entityType,entityId);
        return CommunityUtil.getJSONString(0,"已取消关注");
    }
    // 那么实际上可以对这个网站很多的实体,比如帖子,用户,评论等进行关注,我只实现用户之间的关注

    // 因为是一个查询,请求的方式应该为get
    @RequestMapping(path = "/followees/{userId}",method = RequestMethod.GET)
    public String getFollowees(Model model, @PathVariable("userId")int userId, Page page){
        User user = userService.findUserById(userId);
        // 防止用户故意写一个错误的路径访问
        if(user == null){
            throw new RuntimeException("该用户不存在!");
        }
        // 然后把这个user传入model中,那么为什么需要传入model中呢,明明可以直接在profile中获取到userId,然后动态的将userId拼入关注列表就行了吗?
        // 因为我们的关注列表页,还有一个关注username的人,和username关注的人两个标签,所以就是说关注列表/followees/{userId}也同样需要user对象的传入
        model.addAttribute("user",user);
        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int)followService.findFolloweeCount(userId,ENTITY_TYPE_USER));

        List<Map<String,Object>> userList = followService.findFollowees(userId,page.getOffset(),page.getLimit());
        // 还需要有个补充,判断当前登录用户是否对查询关注列表用户的关注列表内的用户关注过,要有一个关注的状态
        if(userList!=null){
            for (Map<String,Object> map:
                 userList) {
                User u = (User)map.get("user");
                // 判断当前用户对这个用户的关注状态怎么样
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users",userList);
        return "/site/followee";
    }

    @RequestMapping(path = "/followers/{userId}",method = RequestMethod.GET)
    public String getFollowers(Model model, @PathVariable("userId")int userId, Page page){
        User user = userService.findUserById(userId);
        // 防止用户故意写一个错误的路径访问
        if(user == null){
            throw new RuntimeException("该用户不存在!");
        }
        // 然后把这个user传入model中,那么为什么需要传入model中呢,明明可以直接在profile中获取到userId,然后动态的将userId拼入关注列表就行了吗?
        // 因为我们的关注列表页,还有一个关注username的人,和username关注的人两个标签,所以就是说关注列表/followees/{userId}也同样需要user对象的传入
        model.addAttribute("user",user);
        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int)followService.findFollowerCount(ENTITY_TYPE_USER,userId));

        List<Map<String,Object>> userList = followService.findFollowers(userId,page.getOffset(),page.getLimit());
        // 还需要有个补充,判断当前登录用户是否对查询关注列表用户的关注列表内的用户关注过,要有一个关注的状态
        if(userList!=null){
            for (Map<String,Object> map:
                    userList) {
                User u = (User)map.get("user");
                // 判断当前用户对这个用户的关注状态怎么样
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users",userList);
        return "/site/follower";
    }

    private boolean hasFollowed(int userId){
        // 如果当前用户没登录的话,那么是不可能关注列表中的用户的
        if(hostHolder.getUser() == null){
            return false;
        }
        return followService.hasFollowing(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
    }


}
