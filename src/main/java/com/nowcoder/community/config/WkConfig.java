package com.nowcoder.community.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

// 这里面并不会定义什么Bean,因为Wk和Spring没有什么关系也
// 我要做的事情就是在服务启动的时候,保证创建那个存储的目录
// 但那为什么还要使用@Configuration注解呢? 答案在下面
@Configuration
public class WkConfig {

    private static final Logger logger = LoggerFactory.getLogger(WkConfig.class);

    // 注入路径
    @Value("${wk.image.storage}")
    private String wkImageStorage;

    // 写一个初始化的方法,在构造器之后执行
    // 服务启动的时候,因为带有@Configuration注解,那么Spring会认为这是一个配置类,会先去加载它,先去实例化它
    // 实例化的时候就会自动调用@PostConstruct注解的init()方法,所以在启动服务的时候这个方法就会调用一次,这个时机就刚好
    @PostConstruct
    public void init(){
        // 创建wk图片目录
        File file = new File(wkImageStorage);
        if(!file.exists()){
            file.mkdir();
            logger.info("创建WK图片目录: " + wkImageStorage);
        }
    }

}
