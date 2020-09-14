package com.nowcoder.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 指明这个注解用于描述方法
@Target(ElementType.METHOD)
// 指定注解生效的时间,程序运行时有效
@Retention(RetentionPolicy.RUNTIME)
// 注解里面什么都不需要写,只起到标识的作用,打上这个标记那就需要登录才可以访问
public @interface LoginRequired {

}
