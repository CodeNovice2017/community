package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    // 在请求最初判断登陆与否
    // 写好之后还要在配置类中配置,就算是对所有请求路径都要拦截也应该配置一下,因为至少要排除对静态资源的拦截
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 尝试获取用户
        // Object handler参数是我们拦截的目标,要判断这个目标是不是方法,是方法才拦截
        // 通过SpringMVC提供的一个类型HandlerMethod来判断,如果说拦截到的是一个方法的话,那么Object handler这个对象将是这个类型
        if(handler instanceof HandlerMethod){
            // 强制转型,方便使用HandlerMethod类的方法
            HandlerMethod handlerMethod = (HandlerMethod)handler;
            Method method = handlerMethod.getMethod();
            // 然后在方法对象上去取注解,按照指定类型取注解,没有全部获取
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            // 当前方法需要登录,但是User又没有被服务器持有(就是没登录),那么就不可以访问
            if(loginRequired != null && hostHolder.getUser() == null){
                // 利用response对象进行重定向到login页面,这个方法是接口声明的,所以不能像Controller随便return一个模板
                // 重定向的路径需要配置,一种办法可以导入配置文件的domain等配置组合,另一种办法可以直接从request中获取
                response.sendRedirect(request.getContextPath()+"/login");
                // return false拒绝之后的所有请求
                return false;
            }
        }
        return true;
    }
}
