package com.nowcoder.community.config;

import com.nowcoder.community.quartz.AlphaJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

// 配置->仅仅在第一次初始化Quartz Job时被读取到初始化到数据库中->以后Quartz访问数据库不再访问该配置文件
@Configuration
public class QuartzConfig {

    // 为什么一会说是JobDetail,一会又是JobDetailFactoryBean?
    // 因为Spring底层有很多地方都有这样的名字,叫XXXFactoryBean,这个XXXFactoryBean和之前学IoC的BeanFactory有本质的区别
    // IoC的BeanFactory是整个IoC容器的顶层接口,这两个是两码事
    // 这经常是一道笔试面试题
    // FactoryBean的作用是可以简化Bean的实例化过程
    // 就是说有些Bean的实例化非常麻烦,有了FactoryBean就容易得多
    // 或者比如说说这个类JobDetailFactoryBean底层其实封装了JobDetail的详细实例化过程,我们使用JobDetailFactoryBean就简化了流程

    // 1.通过FactoryBean封装Bean的实例化过程.
    // 2.将FactoryBean装配到Spring容器里.(我们要的不是JobDetailFactoryBean,而是JobDetail,但是JobDetailFactoryBean封装了JobDetail实例化的过程)
    // 3.将FactoryBean注入给其他的Bean.
    // 4.该Bean(注入到的那个其他的bean)得到的是FactoryBean所管理的对象实例.

    // 就是相当于我把JobDetailFactoryBean装配到了容器里,
    // 然后在public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaJobDetail)
    // 参数上如果需要JobDetail,那么我就把public JobDetailFactoryBean alphaJobDetail()
    // 这个Bean的名字alphaJobDetail注入进去,Spring管理容器的情况下,我得到就不是JobDetailFactoryBean,而是它内部管理的那个对象,Spring自动帮我们注入了
    // 实际上不一定非要这个alphaJobDetail同名,但是未来可能容器里有很多JobDetailFactoryBean的实例,也可以说是有很多JobDetail实例
    // 也就是说这个JobDetailFactoryBean装配到Spring容器之后,
    // Spring会自动的将其内部实例化的JobDetail这个Bean注入给(被注入了JobDetailFactoryBean这个Bean的其他Bean中使用JobDetail作为参数的方法)(有点绕)

    // 配置JobDetail
//    @Bean
    public JobDetailFactoryBean alphaJobDetail(){
        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
        // 声明管理的是哪个Bean,它的类型是什么
        jobDetailFactoryBean.setJobClass(AlphaJob.class);
        // 声明Job的名字是什么,不要和其他Job重复,否则会有问题
        jobDetailFactoryBean.setName("alphaJob");
        // 给Job一个组,多个任务可以位于一个组,取个组名
        jobDetailFactoryBean.setGroup("alphaJobGroup");
        // 声明一下任务是否是持久的保存,选择是,哪怕是任务将来不再运行了,甚至是触发器都没有了,那么也会一直存着,不用删
        jobDetailFactoryBean.setDurability(true);
        // 任务是否是可恢复的
        jobDetailFactoryBean.setRequestsRecovery(true);
        return jobDetailFactoryBean;
    }
    // 初始化Trigger时,它是依赖于JobDetail的,因为Trigger表中有JobDetail的数据
    // 还有另一个CronTriggerFactoryBean,能配置更复杂的Trigger
    // 配置Trigger
//    @Bean
    public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaJobDetail){
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(alphaJobDetail);
        factoryBean.setName("alphaTrigger");
        factoryBean.setGroup("alphaTriggerGroup");
        // 多长时间执行一次这个任务
        factoryBean.setRepeatInterval(3000);
        // Trigger的底层需要存储Job的一些状态,那需要指定用哪个对象来存,我们就指定一个默认的对象即可,也可以去写一个新的类型
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }

}
