package com.nowcoder.community.controller.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
// HandlerInterceptor共有三个方法,且都做了default默认空实现,所以用哪一个就覆写哪一个方法即可
public class AlphaInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AlphaInterceptor.class);

    // 在Controller之前执行
    // return false就是Controller就不会执行,取消这个请求了
    // 因为是拦截请求,所以参数是有HttpServletRequest,HttpServletResponse对象的,就是如果你想加一些东西在请求和响应中都是可以的
    // 还给了一个Object handler
    // 控制台具体的返回值[AlphaInterceptor.java:25] preHandle: public java.lang.String com.nowcoder.community.controller.LoginController.getLoginPage()
    // 就是说Object handler就是拦截的目标
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        logger.debug("preHandle: " + handler.toString());
        return true;
    }

    // 在调用完Controller以后执行的
    // 除了之前preHandle的三个参数以外,postHandle还有ModelAndView对象,因为这个方法是在Controller之后执行的,也就是说我主要的请求逻辑已经处理完了
    // 下一步就要去模板引擎了,要去给页面返回要渲染的内容了,因此在渲染模板引擎时候,可能你需要装入一些数据
    // 也就是在Controller请求逻辑之后,调用模板引擎之前执行
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        logger.debug("postHandle: " + handler.toString());

    }

    // 在程序最后的执行,就是在模板引擎之后执行
    // 还有一个异常对象,就会如果说调用Controller,调用模板过程中出现异常,这里可以获取到异常信息
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

        logger.debug("afterCompletion: " + handler.toString());

    }
}
