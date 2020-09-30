package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
@RequestMapping(path = "/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @LoginRequired
    @RequestMapping(path = "/setting",method = RequestMethod.GET)
    public String getSettingPage(){

        return "/site/setting";

    }

    @LoginRequired
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    // 使用SpringMVC提供的一个专有的类型接收图像文件,如果页面传入多个,可以写成数组
    public String uploadHeader(MultipartFile headerImage, Model model){

        if(headerImage == null){
            model.addAttribute("error","您还没有选择图片!");
            return "/site/setting";
        }

        // 上传文件,肯定不能按原始文件来存,防止覆盖,但后缀不能变
        // 先暂存文件的后缀
        String fileName = headerImage.getOriginalFilename();
        // 从文件截取后缀,从最后一个.截取
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 有可能用户上传的图片真的没有后缀,那我们就不处理
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件格式不正确!");
            return "/site/setting";
        }

        // 如果图片没问题就生成文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        // 确定文件存放的路径,需要给一个全限定名
        File file = new File(uploadPath + "/" + fileName);
        // 将用户上传的头像写入目标文件中去
        try {
            headerImage.transferTo(file);
        } catch (IOException e) {
            logger.error("上传文件失败:" + e.getMessage());
            // 在加个异常抛出去,打断整个程序,这个异常如果处理,以后会编写一个统一处理模块,统一处理Controller抛出的全部异常
            throw new RuntimeException("上传文件失败,服务器发生异常!",e);
        }

//        更新当前用户的头像路径
        //注意:这个路径可不是本地路径,提供的应该是web访问路径
        //web访问路径目前开发阶段来说应该大体上是http://localhost:8080/community/user/header/xxx.png,系统上线之后ip:port会替换为域名
        User user = hostHolder.getUser();
        // 允许外界访问的web路径
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeaderUrl(user.getId(),headerUrl);
        // 更换成功就重定向到首页
        return "redirect:/index";
    }

    // 获取头像
    @RequestMapping(path = "/header/{fileName}",method = RequestMethod.GET)
    // 这个方法返回值为void,因为这个方法比较特别,因为它向浏览器响应的不是一个网页,是一个二进制数据的图片,所以要通过流主动向浏览器输出,要手动输出到Response,不需要视图
    public void getHeader(@PathVariable("fileName")String fileName, HttpServletResponse httpServletResponse){
        // 先要找到服务器存放图片的路径
        // 带上本地路径的变量,全限定名
        fileName = uploadPath + "/" + fileName;
        // 浏览器输出的是这个图片,输出的时候需要先声明我输出的文件格式是什么
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片,contentType格式固定image/文件后缀
        httpServletResponse.setContentType("image/" + suffix);
        // 因为响应的是二进制数据,所以需要字节流
        try (
                FileInputStream fileInputStream = new FileInputStream(fileName);
                OutputStream outputStream = httpServletResponse.getOutputStream();
        ){
            // 获得输出流,同时要知道这么获得的输出流是Spring MVC自动管理的,他会自动的关闭,但是后面的输入流是我们自己new的,他不会帮我们管理输入流
            // 所以要使用jdk7的特性,try加()将输入流写进括号,这样编译的时候,编译器会自动帮我们加finally,只要这个new的对象是有close()方法的,显然输入流是有的
            // 顺手把输出流也放里面了,但是要知道输出流是可以不用放()里面的

            // 不要一个一个字节输出,建立一个缓冲,一批一批输出效率会更高,1024个字节的缓冲区
            byte[] buffer = new byte[1024];
            int b = 0;
            // 每次read最多buffer缓冲区大小,用b接收读取结果,b等于-1就是没读到数据
            while((b = fileInputStream.read(buffer)) != -1){
                outputStream.write(buffer,0,b);
            }

        } catch (IOException e) {
            logger.error("读取头像失败:" + e.getMessage());
        }
    }

    @LoginRequired
    // 修改密码
    @RequestMapping(path = "/updatePassword",method = RequestMethod.POST)
    public String updatePassword(String oldPassword,String newPassword,String ensurePassword,Model model){

        User user = hostHolder.getUser();
        Map<String,Object> map = userService.updateUserPassword(user,oldPassword,newPassword,ensurePassword);
        if(map == null || map.isEmpty()){
            return "redirect:/index";
        }else{
            model.addAttribute("oldPasswordMsg",map.get("oldPasswordMsg"));
            model.addAttribute("newPasswordMsg",map.get("newPasswordMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/setting";
        }
    }

    // 个人主页
    // 不只是显示当前用户的主页,也是显示任意用户的主页
    // 所以我添加参数到路径中,直接通过userId为参数来查询
    @LoginRequired
    @RequestMapping(path = "/profile/{userId}",method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId")int userId,Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new IllegalArgumentException("该用户不存在!");
        }

        // 用户
        model.addAttribute("user",user);
        // 用户获赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);
        // 目标用户关注数量
        long followeeCount = followService.findFolloweeCount(userId,ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        // 目标用户粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER,userId);
        model.addAttribute("followerCount",followerCount);
        // 当前登录用户对目标用户是否已关注
        boolean hasFollow = false;
        if(hostHolder.getUser()!=null){
            hasFollow = followService.hasFollowing(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollow",hasFollow);
        return "/site/profile";
    }

}
