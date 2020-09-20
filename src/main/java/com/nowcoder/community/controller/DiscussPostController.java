package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
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

import java.util.*;

@Controller
@RequestMapping(path = "/discuss")
public class DiscussPostController implements CommunityConstant{

    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

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
    // 为了评论的分页,传入Page page,只要是entity包下的实体类型,一个java bean(并不是由Spring管理的bean)声明在参数当中,最终Spring MVC都会把这个java bean放在Model里,就可以直接在页面获取
    public String getDiscussPost(Model model, @PathVariable("discussPostId") int discussPostId, Page page){
        // 查询帖子
        DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post",discussPost);
        User user = userService.findUserById(discussPost.getUserId());
        model.addAttribute("user",user);

        // 帖子评论的处理也应该是在这个请求下完成的
        // 查评论的分页信息
        page.setLimit(5); // 每页先显示5条评论,因为数据库数据较少
        // 因为到时候是通过page.path传入路径给thymeleaf模板引擎中,所以只需要到时候也使用@{page.path}即可自动补全路径
        page.setPath("/discuss/detail/" + discussPostId);
        // CommentCount在数据库discuss_post表中,我们冗余了CommentCount字段用以提供每个帖子的评论数,空间换时间
        page.setRows(discussPost.getCommentCount());

        // 最好将entity_type配置为常量
        // 这是给帖子的评论的列表,只返回了ENTITY_TYPE_POST
        List<Comment> commentList = commentService.findCommentsByEntity(ENTITY_TYPE_POST, discussPost.getId(),page.getOffset(),page.getLimit());

        // 老办法,遍历这个集合,构造一个map,然后把comment存进去,依赖的数据存进去就OK
        // 就是用Map对要展现的数据做一个统一的封装(也就是把每个map作为一个封装,每一个map封装一个comment评论对象和user对象等)
        // commentVo就是View Object显示对象
        // 评论的显示对象列表
        // map就是一个评论的Vo
        List<Map<String,Object>> commentVoList = new ArrayList<>();
        if(commentList != null){
            for(Comment comment : commentList){
                // 评论VO(之后循环的东西都是先向commentVo里面存,然后最后存入commentVoList)
                Map<String,Object> commentVo = new HashMap<>();
                // 评论
                commentVo.put("comment",comment);
                // 作者
                commentVo.put("user",userService.findUserById(comment.getUserId()));
                // 不只是帖子有评论,帖子的评论也有评论
                // 普通的评论,就是给帖子的评论,我们称为评论
                // 给评论的评论以后称为回复

                // 回复列表
                // 评论的评论就不用分页了,一个页面两层分页就比较恶心了,所以直接有多少查多少,从第0条开始查
                List<Comment> replyList = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT,comment.getId(),0, Integer.MAX_VALUE);

                // 回复也需要User,所以还需要回复的VO列表
                List<Map<String,Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply: replyList) {
                        Map<String,Object> replyVo = new HashMap<>();
                        replyVo.put("reply",reply);
                        replyVo.put("user",userService.findUserById(reply.getUserId()));
                        // 还需要处理target_id,刚才帖子的评论不需要,但是回复是需要的,有指向的
                        // 判断回复的目标
                        // 如果等于0,就代表是
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target",target);
                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys",replyVoList);

                // 回复数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments",commentVoList);
        return "/site/discuss-detail";
    }



}
