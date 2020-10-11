package com.nowcoder.community.controller.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
//@Aspect
public class ServiceLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    @Autowired
    private SimpleDateFormat simpleDateFormat;

    // 方面组件要定义切点,具体织入到bean的哪些位置
    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")
    // "execution(* com.nowcoder.community.service.*.*(..))" 固定的一个关键字execution,
    // 然后* 代表方法的返回值(*表示什么返回值都可以,我不关注返回值),然后是com.nowcoder.community.service包名下的*所有service组件的*所有的方法(..)所有的参数都要处理
    public void pointCut(){

    }

    @Before("pointCut()")
    public void before(JoinPoint joinPoint){
        // 用户[192.168.1.1],在[xxx]时间访问了[com.nowcoder.community.service.xxx()]

        // 用户ip的获取
        // 在这里就不能声明个参数,Spring MVC自动传入了
        // RequestContextHolder.getRequestAttributes()的返回对象强转为其子类型ServletRequestAttributes,这样提供的方法更多一些
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        // 如果servletRequestAttributes==null的话,表示这是一个特殊的调用
        // (比如kafka的生产者和消费者,消费者EventConsumer调用了Service,但实际上它不是一个Controller,自然也没有请求的路径,自然也获取不到request对象)
        // 这里就可以进行判断,如果是特殊的Service调用,那么就可以不要试图获取ip了,或者直接不记日志了
        if(servletRequestAttributes == null){
            return;
        }
        HttpServletRequest httpServletRequest = servletRequestAttributes.getRequest();
        String ip = httpServletRequest.getRemoteHost();

        String now = simpleDateFormat.format(new Date());

        // 然后就需要获取哪个类的哪个方法
        // 那么实际上除了环绕通知以外的before等通知也可以加连接点的参数,只不过名字不同
        // 连接点指代的程序织入的目标,目标组件要调用的那个方法,指代的是这个方法
        // 通过joinPoint就可以得到调用的是哪个类哪个方法
        String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();

        logger.info(String.format("用户[%s],在[%s],访问了[%s].",ip,now,target));
    }

}
