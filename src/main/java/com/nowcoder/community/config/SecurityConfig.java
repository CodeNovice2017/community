package com.nowcoder.community.config;

import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }

    // 这个认证因为项目本身已经完成了,就不覆写了,之后会配置绕过Security自带的认证
    /*
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

    }
    */

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 授权
        http.authorizeRequests()
                .antMatchers(
                        // 不登录不能使用用户设置
                        "/user/setting",
                        // 不能上传头像
                        "/user/upload",
                        // 不能发帖
                        "/discuss/add",
                        // 不能添加评论,**是因为有帖子id需要填加
                        "/comment/add/**",
                        // 不能使用私信所有的功能
                        "/letter/**",
                        // 不能使用通知的所有功能
                        "/notice/**",
                        // 不能点赞
                        "/like",
                        // 不能关注
                        "/follow",
                        // 不能取消关注
                        "/unfollow"
                ).hasAnyAuthority(
                    AUTHORITY_USER,
                    AUTHORITY_ADMIN,
                    AUTHORITY_MODERATOR
                // 除了上面路径以外的所以路径是所有人都可以访问的
                ).anyRequest().permitAll()
                .and().csrf().disable();

        // 权限不够的处理
        http.exceptionHandling()
                // 测试的Demo是使用的accessDeniedPage()简单的配置了没有权限时的错误页面
                // 但我们现在是一个正式的项目,如果还这样配置就比较简陋了,因为我们当前的项目有多种页面和请求,有的请求时普通请求,有的是异步请求
                // 普通请求他希望服务器返回的是一个Html,我们直接指定一个错误页面还可以,但是如果是异步请求,浏览器想要服务器返回的是一个JSON
                // 所以需要分别的对待

                // 没有登录时的处理
                // 先配置一下authenticationEntryPoint,这个是配置没有登录的时候怎么处理,也就是发现没登录需要进到这个入口去处理
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
                        // 如果是普通的请求,那就跳转到登录页面让用户去登录
                        // 如果是异步请求,拼一个JSON字符串,然后浏览器上给一个提示或者通过前端的手段引导用户去登陆
                        // 如何判断当前请求是普通还是异步呢?
                        // 主要是看请求消息头的某个值
                        String xRequestWith = httpServletRequest.getHeader("x-request-with");
                        // 异步请求期待返回的是xml,异步请求ajax里面的x代表xml,但现在被JSON给代替了
                        if("XMLHttpRequest".equals(xRequestWith)){
                            // 设置ContentType为"application/plain"表示是普通字符串,但是我们要确保这个字符串是JSON格式
                            // 这样前台才能够解析
                            httpServletResponse.setContentType("application/plain;charset=utf8");
                            PrintWriter writer = httpServletResponse.getWriter();
                            // 为了返回异步请求的时候,肯定要用到我们预先定义好的JSON返回工具了CommunityUtil.getJSONString()方法
                            // 当没有权限的时候,服务器拒绝访问的时候,而不是服务器报错的时候,通常返回状态码403
                            writer.write(CommunityUtil.getJSONString(403,"你还没有登录哦!"));
                        }else{
                            httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/login");
                        }
                    }
                })
                // 权限不足时的处理
                // accessDeniedHandler权限不足的时候如何处理
                .accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException, ServletException {
                        // 如果是普通的请求,那就跳转到登录页面让用户去登录
                        // 如果是异步请求,拼一个JSON字符串,然后浏览器上给一个提示或者通过前端的手段引导用户去登陆
                        // 如何判断当前请求是普通还是异步呢?
                        // 主要是看请求消息头的某个值
                        String xRequestWith = httpServletRequest.getHeader("x-request-with");
                        // 异步请求期待返回的是xml,异步请求ajax里面的x代表xml,但现在被JSON给代替了
                        if("XMLHttpRequest".equals(xRequestWith)){
                            // 设置ContentType为"application/plain"表示是普通字符串,但是我们要确保这个字符串是JSON格式
                            // 这样前台才能够解析
                            httpServletResponse.setContentType("application/plain;charset=utf8");
                            PrintWriter writer = httpServletResponse.getWriter();
                            // 为了返回异步请求的时候,肯定要用到我们预先定义好的JSON返回工具了CommunityUtil.getJSONString()方法
                            // 当没有权限的时候,服务器拒绝访问的时候,而不是服务器报错的时候,通常返回状态码403
                            writer.write(CommunityUtil.getJSONString(403,"你没有访问此功能的权限!"));
                        }else{
                            // 权限不足不是无法登陆,所以要跳转到权限不足的页面
                            httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/denied");
                        }
                    }
                });

        // Spring Security管的范围很多,默认情况下,Security就会拦截名为logout的退出请求,自动拦截做了处理
        // 因为Spring Security的底层都是通过Filter去拦截做权限处理,Filter代码在DispatcherServlet之前就执行了,肯定是在Controller之前
        // 如果Spring Security帮我们处理了/logout,那么处理结束后,程序就不自动向下走了,我们自己写的/logout对应的Controller方法就不会被执行了
        // 那么这里我想走我们自己写的/logout逻辑
        // 覆盖它默认的退出逻辑,就可以执行我们自己的退出代码
        // 可以查看logoutUrl的源码,底层就是直接拦截/logout路径,那么我们直接设置一个其他的路径,让Spring Security拦截一个没有的退出路径,就不会触发,就绕过了
        // 善意欺骗
        http.logout()
                .logoutUrl("/securityLogout");

    }
}
