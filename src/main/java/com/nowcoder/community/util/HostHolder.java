package com.nowcoder.community.util;

import com.nowcoder.community.entity.User;
import org.springframework.stereotype.Component;

// 习惯叫HostHolder
// 这个工具实际就是起到一个容器的作用
// 用于代替Session对象,实际上session对象就是线程隔离的
// 用于持有对象
// ThreadLocal也是面试考察多线程的重点,需要好好研究,面试的时候可以引导项目问题到这里
@Component
public class HostHolder {

    // 里面存的是每个线程对应的user
    // ThreadLocal就是提供了一个get取值一个set存值,源码也是很简单的实现了线程隔离
    // 比如看set源码,就是获取当前线程,然后获取一个map对象,每个线程的map对象不同,所以实现了线程隔离
    // 说白了就是以线程为map的key存取值的
    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user){
        users.set(user);
    }

    public User getUser(){
        return users.get();
    }

    // 清理,请求结束之后将ThreadLocal中的User对象清理掉,否则只加不清理,那么占用的内存资源会越来越大
    public void clear(){
        users.remove();
    }

}
