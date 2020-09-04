package com.nowcoder.community.service;

import com.nowcoder.community.dao.AlphaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

//该业务类用于测试观察Spring IoC IoC容器的对象创建,销毁的过程

//业务组件
@Service
//@Scope("prototype")
public class AlphaService {

    @Autowired
    private AlphaDao alphaDao;

    public AlphaService(){
        System.out.println("实例化AlphaService");
    }

    //该注解的意思是,这个方法会在构造之后调用,也就是这个初始化方法会在构造器构造之后调用
    @PostConstruct
    public void init(){
        System.out.println("初始化AlphaService");
    }

    //在对象被销毁之前调用这个方法的注解
    @PreDestroy
    public void destroy(){
        System.out.println("销毁AlphaService1");
    }

    //写一个方法模拟查询业务
    //也就是service依赖于dao的依赖注入方式
    public String find(){
        return alphaDao.select();
    }


}
