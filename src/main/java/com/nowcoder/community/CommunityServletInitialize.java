package com.nowcoder.community;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

public class CommunityServletInitialize extends SpringBootServletInitializer {

    // 就是说Tomcat会访问继承了SpringBootServletInitializer的CommunityServletInitialize类的SpringApplicationBuilder方法
    // 通过这个方法来作为入口来运行这个项目,运行的时候是声明我们的主配置文件是谁
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(CommunityApplication.class);
    }
}
