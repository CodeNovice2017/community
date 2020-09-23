package com.nowcoder.community.controller.advise;

import com.nowcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

// 使用这个组件的好处就是,我们不用对任何一个Controller去处理,不用再任何Controller上加代码,就能解决我们的问题
@ControllerAdvice(annotations = Controller.class)
// 如果只这么配置的话,这个组件会扫描所有的bean,范围太大
// 填些annotations = Controller.class代表让这个组件只去扫描带有Controller注解的bean
public class ExceptionAdvise {


    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvise.class);

    // 加一个方法,统一处理错误
    // 要添加一个注解ExceptionHandler,表示这个方法是处理所有异常的方法,
    // 可以用一个{}内写上要处理的异常类型,也可以{Exception.class},Exception是所有异常的父类
    @ExceptionHandler({Exception.class})
    // 这个方法可以带很多很多参数,可以在手册查,通常一般这三个就够了,Exception就是Controller当中发生的异常,
    // 当异常发生时,会发这个异常传进来
    public void handleException(Exception exception, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        // 当这个方法被调用的时候,Controller肯定是发生异常了,那么我们就把异常记到日志中
        logger.error("服务器发生异常: " + exception.getMessage());
        // 上面只是一个记录了一个概括,如果想把详细的栈的信息都记录下来,就要遍历栈信息,这是一个exception.getStackTrace()数组
        for (StackTraceElement stackTraceElement : exception.getStackTrace()){
            logger.error(stackTraceElement.toString());
        }
        // 记录日志信息之后要重定向到错误页面
        // 但是要注意,浏览器访问服务器有可能是普通请求,希望返回网页,那么重定向到500没有什么问题
        // 但是也有可能是异步请求,想要返回的是一个JSON,那就要做区分处理
        // 所以要判断普通请求或是异步请求,作为固定的技巧记住

        String xRequestedWith = httpServletRequest.getHeader("x-requested-with");
        // 只有异步请求才希望返回XML,当然也可以返回JSON和其他数据,否则普通请求返回的是HTML
        if("XMLHttpRequest".equals(xRequestedWith)){
            // 可以setContentType("application/json");我们就是向浏览器返回一个JSON字符串,浏览器自动转成JSON对象
            // 也可以写setContentType("application/plain"); 代表向浏览器返回的是普通的字符串,但是可以是JSON格式的,浏览器得到以后需要人为的转换为JSON对象,就是用$.parseJSON
            // 在我们的项目中,我们确认返回的都是普通的字符串,但格式是JSON的,然后我们手动转
            httpServletResponse.setContentType("application/plain;charset=utf-8");
            PrintWriter printWriter = httpServletResponse.getWriter();
            printWriter.write(CommunityUtil.getJSONString(1,"服务器异常!"));
        } else{
            httpServletResponse.sendRedirect(httpServletRequest.getContextPath()+ "/error");
        }

    }

}
