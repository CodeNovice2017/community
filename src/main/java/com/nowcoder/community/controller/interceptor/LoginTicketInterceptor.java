package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CookieUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

// 在请求开始之初就找到了用户信息,并暂存在了HostHolder对象(工具包下)当中
// 要在模板引擎调用之前就要用这个信息,所以要在模板引擎调用之前装入Model当中
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    // 应该在请求的最开始就完成用户信息的获取
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 第一步是通过Cookie得到ticket
        // 同时注意,这个方法是由接口定义的,不能加个参数之类的,不能加个参数然后声明@CookieValue()注解了,只能通过Cookie[]数组来遍历获取
        // 那我们就将从request中获取cookie的这个功能封装一下,方便以后复用
        String ticket = CookieUtil.getValue(request,"ticket");
        if(ticket!=null){
            // 查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            // 检查凭证是否有效,loginTicket.getExpired().after(new Date())超时时间晚于当前时间
            if(loginTicket!=null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())){
                User user = userService.findUserById(loginTicket.getUserId());
                // 查到以后是在模板上用,在Controller处理业务时也可能会用
                // 为了后面的使用,需要暂存一下User对象,或者说是在本次请求中持有用户
                // 那么这个对象的暂存就有说法了,怎么存储呢,有的人可能说可以存在容器中,但是要考虑的是浏览器和服务器是多对1的方式,一个服务器并发的处理多个请求
                // 每个浏览器访问服务器,服务器都会创建一个线程来解决请求,服务器在处理请求时,是多线程的环境,所以要存用户的时候要考虑到多线程的问题
                // 那么就要考虑线程的隔离,在多个线程之间隔离存储对象
//                ThreadLocal也是面试考察多线程的重点,需要好好研究,面试的时候可以引导项目问题到这里
                // 这个逻辑也封装一个小的工具

                // 为什么能够持有呢?
                // 我们在这里就将数据存到了当前线程对应的map里面,这个请求只要没处理结束,这个线程就一直还在,当请求处理完,服务器向浏览器做出响应之后,这个线程被销毁
                // 所以在整个处理请求的过程中,在后续,这个数据都是一直活着的,也就是ThreadLocal内的数据是一直都在的,而且提供了线程隔离

                hostHolder.setUser(user);

                // 构建用户认证的结果,并存入SecurityContext,以便于Security进行授权
                Authentication authentication = new UsernamePasswordAuthenticationToken(user,user.getPassword(), userService.getAuthorities(user.getId()));
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
            }
        }
        return true;
    }

    // postHandle就是在模板调用之前,Controller逻辑完成之后调用的
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if(user != null && modelAndView != null){
            modelAndView.addObject("loginUser",user);
        }
    }

    // 在整个请求结束的时候,模板都执行之后清理HostHolder的数据
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
        SecurityContextHolder.clearContext();
    }
}
