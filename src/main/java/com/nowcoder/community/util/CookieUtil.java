package com.nowcoder.community.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieUtil {

    // 不使用容器了,直接用静态方法
    public static String getValue(HttpServletRequest httpServletRequest,String name){
        if(httpServletRequest == null || name == null){
            throw new IllegalArgumentException("参数为空,CookieUtil方法无法处理!");
        }
        // 一下获取所有的cookie对象,获得的是一个数组
        Cookie[] cookies = httpServletRequest.getCookies();
        if(cookies!=null){
            for (Cookie cookie: cookies) {
                if(cookie.getName().equals(name)){
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

}
