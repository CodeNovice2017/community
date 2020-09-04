package com.nowcoder.community.config;

//该类作用是学习测试如何导入管理第三方bean

//通常@SpringBootApplication这里虽然也可以填上作为配置类,但是一般情况下SpringBootApplication是在主函数入口的

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

@Configuration
public class AlphaConfig {

    //加入java官方自带的simpleDateFormat,把这个simpleDateFormat交由Spring管理,实例化一次后放在容器里,可以反复在各处使用
    //那么此时simpleDateFormat方法名实际就也是bean的名字
    //这和前面的AlphaService和AlphaDao不同,是因为本身AlphaDao就是一个bean(本身就声明为Repository,而这个SimpleDateFormat是第三方的组件,在配置类AlphaConfig中进行了声明)
    //这样做的好处就是统一将第三方的组件都在此配置类中配置,然后交由Spring作为bean管理,在其他任何地方需要使用就不需要重新声明,不需要new对象,直接@Autowired注入依赖,各处的simpleDateFormat也都使用这个单例实例
    @Bean
    public SimpleDateFormat simpleDateFormat(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

}
