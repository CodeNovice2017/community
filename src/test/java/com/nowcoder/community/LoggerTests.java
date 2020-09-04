package com.nowcoder.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
//通过下面注解实现在测试中也能引用CommunityApplication配置类,这样才接近正式环境,一会运行时的测试代码就是以CommunityApplication为配置类了
@ContextConfiguration(classes = CommunityApplication.class)
public class LoggerTests {
    // 实例化Logger接口
    // 每一个类做日志都单独为其实例化一个logger,不同的类用不同的logger,通常设置为静态的,哪里都能用
    // 有很多的Logger接口,我们选择org.slf4j.Logger
    // 传入一个类,这个类名就是Logger的名字,通常以当前类传进去,这样不同类的Logger也是不同的
    private static final Logger logger = LoggerFactory.getLogger(LoggerTests.class);

    @Test
    public void testLogger(){
        System.out.println(logger.getName());

        //开发的时候一般是用Debug级别,不会开到trace级别,上线之后就开Info级别,一共五个级别,具体见Logback官方文档
        logger.debug("debug log");
        logger.info("info log");
        logger.warn("warn log");
        logger.error("error log");
        // 打印日志之后,还要在配置文件中声明一下我要启用什么样的日志,打印什么样的信息
        // logback也有自己的配置文件,但是经过Spring Boot整合之后也可以在application.properties去配置

    }

}
