package com.nowcoder.community.controller.aspect;

// Spring AOP示例类

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

// 这个实例写完之后,启动项目访问页面,那么每调用一次业务组件,会出现around before -> before -> around after -> after -> afterReturning

// 首先将方面组件声明为一个bean,让容器来管理
// @Component表示这是一个bean,不属于某一层
//@Component
//@Aspect
public class AlphaAspect {

    // 方面组件要定义切点,具体织入到bean的哪些位置
    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")
    // "execution(* com.nowcoder.community.service.*.*(..))" 固定的一个关键字execution,
    // 然后* 代表方法的返回值(*表示什么返回值都可以,我不关注返回值),然后是com.nowcoder.community.service包名下的*所有service组件的*所有的方法(..)所有的参数都要处理
    public void pointCut(){

    }

    // 配置完上面的定义切点之后,就可以编写通知Advise,包括可以在连接点的开始,结束时做什么事情,返回值和抛异常做什么事情等,共5类通知

    // 在连接点的开头记日志,然后传入上面定义的切点,就代表说我是针对这些连接点有效的
    @Before("pointCut()")
    public void before(){
        System.out.println("before");
    }

    // 在连接点的最后记日志,然后传入上面定义的切点,就代表说我是针对这些连接点有效的
    @After("pointCut()")
    public void after(){
        System.out.println("after");
    }

    // 在有了返回值以后处理一些逻辑
    @AfterReturning("pointCut()")
    public void afterReturning(){
        System.out.println("afterReturning");
    }

    // 在抛异常的时候执行一些代码
    @AfterThrowing("pointCut()")
    public void afterThrowing(){
        System.out.println("afterReturning");
    }

    // 既想在前面织入逻辑也想在后面织入逻辑
    // 比较特殊,需要返回值,还需要一个参数ProceedingJoinPoint,这个参数就是连接点,代表目标织入的部位
    @Around("pointCut()")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable{

        // 理解了下面Object object = proceedingJoinPoint.proceed();之后,方面的逻辑如果想在连接点之前执行就写在这句话之前,否则就写在之后

        System.out.println("around before");
        // proceed就是调目标对象被处理的那个方法的逻辑,就是调我们要处理的目标组件的方法
        // 当然了目标组件的方法可能有返回值
        // 因为程序在执行的时候会执行当前代理对象,这个逻辑会被织入代理对象里,用来代替原始对象
        // 这个代码的逻辑是我利用proceedingJoinPoint.proceed();调用原始对象的方法,这样原始对象的方法被调用
        Object object = proceedingJoinPoint.proceed();

        System.out.println("around after");

        return object;

    }
}
