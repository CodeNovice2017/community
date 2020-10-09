package com.nowcoder.community.config;

import com.nowcoder.community.controller.interceptor.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//
@Configuration
public class WebMVCConfig implements WebMvcConfigurer {

    @Autowired
    private AlphaInterceptor alphaInterceptor;

    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

//    @Autowired
//    private LoginRequiredInterceptor loginRequiredInterceptor;

    @Autowired
    private MessageInterceptor messageInterceptor;

    @Autowired
    private DataInterceptor dataInterceptor;

    // 实现这么一个方法,在这个方法里注册拦截器,实际上WebMvcConfigurer接口里有很多的方法,都做了默认的实现,我们可以根据自己的需要进行覆写就行了
    // 注册拦截器bean就是用这么一个方法
    // Spring调用的时候会把InterceptorRegistry对象传进来,利用传入的对象注册Interceptor

    // 写好之后还要在配置类中配置,就算是对所有请求路径都要拦截也应该配置一下,因为至少要排除对静态资源请求的拦截
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 只是这么写一句就是把alphaInterceptor这个bean添加给它,而不是返回一个AlphaInterceptor对象实例了
        // 但是这样只写这一句就是对全部路径都生效
        // 排除静态资源,任浏览器访问,不需要拦截,排除掉静态资源的访问
        // 直接排除/**/*.css,就是项目运行时,localhost:8080/community/css/*.css的路径访问的,所以通过/**/*.css进行排除
        // 配置只拦截register和login的请求
        registry.addInterceptor(alphaInterceptor).excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg").addPathPatterns("/register","/login");
        registry.addInterceptor(loginTicketInterceptor).excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
//        registry.addInterceptor(loginRequiredInterceptor).excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
        registry.addInterceptor(messageInterceptor).excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
        registry.addInterceptor(dataInterceptor).excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
    }

}
