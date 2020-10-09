package com.nowcoder.community;

import com.nowcoder.community.service.AlphaService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
//通过下面注解实现在测试中也能引用CommunityApplication配置类,这样才接近正式环境,一会运行时的测试代码就是以CommunityApplication为配置类了
@ContextConfiguration(classes = CommunityApplication.class)
public class ThreadPoolTest {

    // 先声明个Logger
    // 使用线程去输出内容的时候最好用Logger去输出内容,因为Logger输出内容的时候自然而然会带上线程的Id,而且会有时间
    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolTest.class);

    // JDK普通线程池
    // JDK总共有五个,这里只演示两个常用的
    // 直接实例化一下,方便之后直接用
    // 固定大小5个的线程池,反复复用这5个已经创建好的线程
    private ExecutorService executorService = Executors.newFixedThreadPool(5);

    // JDK带的可执行定时任务的线程池
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

    // 初始化Spring的两个线程池,其实Spring的线程池不需要我们初始化,Spring框架就会帮我们初始化好了,会把初始化好的线程池放入容器里,我们只要注入即可
    // Spring普通线程池
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    // Spring可执行定时任务的线程池
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Autowired
    private AlphaService alphaService;

    // 写两个测试方法演示这两个线程池如何去用
    // 注意:目前用的是Junit测试,Junit测试方法不同于main方法
    // 如果在一个main里启动一个线程,如果这个线程不挂掉的话,这个main会等待这个线程执行,不会立刻结束
    // 但是Junit方法不同,它启动一个线程,它启动的子线程和当前线程是并发的
    // 那么test方法如果后面没有逻辑的话,它就会立刻结束了,不会管启动的另一个线程完没完成
    // 解决办法就是让test线程也就是主线程sleep一会,阻塞一会,而sleep又老是抛异常,所以简单封装一下
    private void sleep(long ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    // 演示两个线程池的使用
    // JDK普通线程池
    @Test
    public void testExecutorService(){
        // 线程池需要给它一个任务让它去执行,然后它会分配一个线程去执行这个任务,这个任务通常称为线程体
        // 我们通常都是让任务实现一个Runnable接口来作为线程体,来提供任务
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("Hello ExecutorService");
            }
        };
        for (int i = 0; i < 10; i++) {
            // 用线程池执行任务,执行多次,共执行10次
            executorService.submit(task);
            // 执行之后就会有下面类似的输出,会显示线程的名字,线程池的名字,而且会在1-5线程反复的调用,确实能看出来这是一个多线程执行环境
//            2020-10-09 18:11:10,660 DEBUG [pool-1-thread-1] c.n.c.ThreadPoolTest [ThreadPoolTest.java:56] Hello ExecutorService
//            2020-10-09 18:11:10,660 DEBUG [pool-1-thread-4] c.n.c.ThreadPoolTest [ThreadPoolTest.java:56] Hello ExecutorService
//            2020-10-09 18:11:10,660 DEBUG [pool-1-thread-3] c.n.c.ThreadPoolTest [ThreadPoolTest.java:56] Hello ExecutorService
        }
        sleep(10000);
    }

    // JDK定时任务线程池
    @Test
    public void testScheduledExecutorService(){
        // 线程池需要给它一个任务让它去执行,然后它会分配一个线程去执行这个任务,这个任务通常称为线程体
        // 我们通常都是让任务实现一个Runnable接口来作为线程体,来提供任务
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("Hello ScheduledExecutorService");
            }
        };
        // 我们经常调用scheduleAtFixedRate以固定的频率执行,最开始延迟10000ms(第一次执行前),每隔1000ms执行
        // 这个就不用像上面调用多次了,这一个线程池调用这一次就会反复执行
        scheduledExecutorService.scheduleAtFixedRate(task, 10000, 1000, TimeUnit.MILLISECONDS);
        sleep(30000);
    }

    // 3.Spring普通线程池
    @Test
    public void testThreadPoolTaskExecutor() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("Hello ThreadPoolTaskExecutor");
                // 输出结果类似下面
//                2020-10-09 18:43:03,414 DEBUG [task-3] c.n.c.ThreadPoolTest [ThreadPoolTest.java:111] Hello ThreadPoolTaskExecutor
//                2020-10-09 18:43:03,413 DEBUG [task-2] c.n.c.ThreadPoolTest [ThreadPoolTest.java:111] Hello ThreadPoolTaskExecutor
//                2020-10-09 18:43:03,413 DEBUG [task-1] c.n.c.ThreadPoolTest [ThreadPoolTest.java:111] Hello ThreadPoolTaskExecutor
                // 其实和JDK的类似,但是Spring线程池的好处是,Spring线程池可以配置最大线程数量,超出还有一个队列的缓冲,整体更灵活,用的话优先用这个更好
            }
        };

        for (int i = 0; i < 10; i++) {
            taskExecutor.submit(task);
        }

        sleep(10000);
    }

    // 4.Spring定时任务线程池
    @Test
    public void testThreadPoolTaskScheduler() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                logger.debug("Hello ThreadPoolTaskScheduler");
            }
        };

        Date startTime = new Date(System.currentTimeMillis() + 10000);
        // 这个第二个参数是传入一个具体的时间Date
        taskScheduler.scheduleAtFixedRate(task, startTime, 1000);

        sleep(30000);
    }

    // 其实上面的两种线程池都有一个简便的调用方式,就是我只要在任意的bean里声明一个方法,该写什么逻辑就写什么逻辑,
    // 然后主要在这个方法之上加上一个注解@Async,那么这个方法就可以在Spring线程池这种环境下去运行,或者说在bean中写的那个方法
    // 我们就可以把它作为一个线程体,这样就更灵活简洁了
    // 比如执行AlphaService中的一个方法
    // 让该方法在多线程环境下,被异步的调用,也就是说我启动一个线程调用这个方法,那么这个方法和主线程是并发执行的,是异步执行的
//    @Async
//    public void execute1() {
//        logger.debug("execute1");
//    }

    // 5.Spring普通线程池(简化)
    @Test
    public void testThreadPoolTaskExecutorSimple() {
        for (int i = 0; i < 10; i++) {
            alphaService.execute1();
        }

        sleep(10000);
    }

    // Spring定时任务的线程池也有简化的方法,也类似Spring普通的线程池的原理
    // 就是注解变成了@Scheduled(initialDelay = 10000, fixedRate = 1000)
    // 同时这个方法我们不需要做什么,只要在bean的方法上加了这个注解,那么它自动就会去调用了
    // 因为是测试环境,需要把项目运行起来,阻塞某一个任务,让整个项目有一个任务在跑,项目没有结束即可
    /*@Scheduled(initialDelay = 10000, fixedRate = 1000)*/
//    public void execute2() {
//        logger.debug("execute2");
//    }

    // 6.Spring定时任务线程池(简化)
    @Test
    public void testThreadPoolTaskSchedulerSimple() {

        sleep(30000);
    }

}
