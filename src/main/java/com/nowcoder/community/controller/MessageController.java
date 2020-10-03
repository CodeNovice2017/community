package com.nowcoder.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController implements CommunityConstant{

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @RequestMapping(path = "/letter/list",method = RequestMethod.GET)
    public String getLetterList(Model model, Page page){

        User user = hostHolder.getUser();
        // 设置分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));

        // 会话列表
        List<Message> conversationList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        // 还要显示未读数量,每一次会话未读数量,会话包含几条私信等额外内容,所以需要包装
        List<Map<String,Object>> conversationVoList = new ArrayList<>();
        if(conversationList != null){
            for (Message message:
                 conversationList) {
                Map<String,Object> map = new HashMap<>();
                // 先把message先存入,为了和业务实际对应,所以key取名为conversation
                map.put("conversation",message);
                map.put("unreadCount",messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                map.put("letterCount",messageService.findLetterCount(message.getConversationId()));
                // 还要显示非当前用户,也就是与之私信的用户的头像,或者是from的,或者是to的
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));
                conversationVoList.add(map);
            }
        }
        model.addAttribute("conversations",conversationVoList);

        // 查询未读消息数量,第二个参数为空即可
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);

        // 未读通知的数量
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);

        return "/site/letter";
    }

    @RequestMapping(path = "/letter/detail/{conversationId}",method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId,Page page,Model model){
        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        // 私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letters = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", message);
                map.put("fromUser", userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }

        model.addAttribute("letters", letters);

        // 私信目标
        model.addAttribute("target", getLetterTarget(conversationId));
        // 将未读的消息提取,然后设置为已读
        List<Integer> ids = getLetterIds(letterList);
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }

        return "/site/letter-detail";

    }

    private List<Integer> getLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();

        if (letterList != null) {
            for (Message message : letterList) {
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }

        return ids;
    }

    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }
    }

    @RequestMapping(path = "/letter/send",method = RequestMethod.POST)
    // 异步请求
    @ResponseBody
    public String addLetter(String targetName,String content){

        User target = userService.findUserByUsername(targetName);
        if (target == null) {
            return CommunityUtil.getJSONString(1, "目标用户不存在!");
        }

        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());

        if (message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.addLetter(message);

        // 没有报错就给页面返回一个状态0即可
        // 如果报错将来统一处理这个问题
        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/notice/list",method = RequestMethod.GET)
    public String getNoticeList(Model model){
        User user = hostHolder.getUser();

        // 查询评论类通知
        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
        // 查到数据以后,需要补充一些数量,user之类的数据,所以要做一个聚合,同时因为是一条数据,所以不用set,而使用map
        Map<String,Object> messageVo = new HashMap<>();
        if(message!=null){
            messageVo.put("message",message);
            // 我们需要将content字段的JSON字符串还原为Event对象才好使用
            // 同时这个content内容再之前EventConsumer序列化的时候,像引号之类的都是用的转义字符&quot,所以最好要还原回来
            // 之前用过HtmlUtils.htmlEscape转义,那也可以使用HtmlUtils.htmlUnescape来反转
            String content = HtmlUtils.htmlUnescape(message.getContent());
            // 之前序列化为JSON对象就是HashMap类型转过来的
            Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVo.put("user",userService.findUserById((Integer)data.get("userId")));
            messageVo.put("entityType",data.get("entityType"));
            messageVo.put("entityId",data.get("entityId"));
            messageVo.put("postId",data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            messageVo.put("count",count);
            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
            messageVo.put("unread",unread);
        }else{
            messageVo.put("message",null);
        }
        model.addAttribute("commentNotice",messageVo);
        // 查询点赞类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);
        // 查到数据以后,需要补充一些数量,user之类的数据,所以要做一个聚合,同时因为是一条数据,所以不用set,而使用map
        messageVo = new HashMap<>();
        if(message!=null){
            messageVo.put("message",message);
            // 我们需要将content字段的JSON字符串还原为Event对象才好使用
            // 同时这个content内容再之前EventConsumer序列化的时候,像引号之类的都是用的转义字符&quot,所以最好要还原回来
            // 之前用过HtmlUtils.htmlEscape转义,那也可以使用HtmlUtils.htmlUnescape来反转
            String content = HtmlUtils.htmlUnescape(message.getContent());
            // 之前序列化为JSON对象就是HashMap类型转过来的
            Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVo.put("user",userService.findUserById((Integer)data.get("userId")));
            messageVo.put("entityType",data.get("entityType"));
            messageVo.put("entityId",data.get("entityId"));
            messageVo.put("postId",data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            messageVo.put("count",count);
            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE);
            messageVo.put("unread",unread);
        } else{
            messageVo.put("message",null);
        }
        model.addAttribute("likeNotice",messageVo);

        // 查询关注类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        // 查到数据以后,需要补充一些数量,user之类的数据,所以要做一个聚合,同时因为是一条数据,所以不用set,而使用map
        messageVo = new HashMap<>();
        if(message!=null){
            messageVo.put("message",message);
            // 我们需要将content字段的JSON字符串还原为Event对象才好使用
            // 同时这个content内容再之前EventConsumer序列化的时候,像引号之类的都是用的转义字符&quot,所以最好要还原回来
            // 之前用过HtmlUtils.htmlEscape转义,那也可以使用HtmlUtils.htmlUnescape来反转
            String content = HtmlUtils.htmlUnescape(message.getContent());
            // 之前序列化为JSON对象就是HashMap类型转过来的
            Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);
            messageVo.put("user",userService.findUserById((Integer)data.get("userId")));
            messageVo.put("entityType",data.get("entityType"));
            messageVo.put("entityId",data.get("entityId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            messageVo.put("count",count);
            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
            messageVo.put("unread",unread);
        }else{
            messageVo.put("message",null);
        }
        model.addAttribute("followNotice",messageVo);

        // 查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);

        // 查询未读系统通知数量
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);

        return "/site/notice";
    }

    @RequestMapping(path = "/notice/detail/{topic}",method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic") String topic,Model model,Page page){
        User user = hostHolder.getUser();

        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));

        // 查某个主题下的通知
        List<Message> notices = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());

        List<Map<String,Object>> noticeVoList = new ArrayList<>();
        if(notices!=null){
            for(Message notice : notices){
                Map<String,Object> map = new HashMap<>();
                // 通知
                map.put("notice",notice);
                // 内容
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String,Object> data = JSONObject.parseObject(content,HashMap.class);

                // 注意理解这个用户Id可不是user.getId(),因为user.getId()是当前用户的id
                // 而我们需要的是,触发这个事件的用户的用户id,也就是说当前登录用户的通知表,都是通过to_id=userId来查到的
                // 但是触发这个事件的user,是无法通过正常的message表的字段存储的,因为没有这样一个字段专门用于存储,
                // 所以我们设计是将全部的需要显示拼接的条件以及额外的数据都通过JSON字符串存到content字段中,所以自然需要反转JSON为Java对象
                // 然后对其需要的数据进行二次的封装,变成可以直接放入model中使用的值
                map.put("user",userService.findUserById((int)data.get("userId")));
                map.put("entityType",data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                // 不管带不带postId,我都传入,外界不会用这个值,就不会报错
                map.put("postId", data.get("postId"));
                // 通知作者
                // 系统用户
                map.put("fromUser", userService.findUserById(notice.getFromId()));

                noticeVoList.add(map);
            }
        }
        model.addAttribute("notices",noticeVoList);

        // 已读
        // 首先获取需要设置已读的那些id
        List<Integer> ids = getLetterIds(notices);
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }

        return "/site/notice-detail";

    }
}
