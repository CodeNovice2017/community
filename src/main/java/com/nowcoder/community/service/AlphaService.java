package com.nowcoder.community.service;

import com.nowcoder.community.dao.AlphaDao;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

//该业务类用于测试观察Spring IoC IoC容器的对象创建,销毁的过程

//业务组件
@Service
//@Scope("prototype")
public class AlphaService {

    @Autowired
    private AlphaDao alphaDao;

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DiscussPostMapper discussPostMapper;

    // 这个TransactionTemplate就是编程式事务的写法,而且是由Spring容器自动管理的
    // 利用这个bean执行的sql就能保证四个特性ACID
    @Autowired
    private TransactionTemplate transactionTemplate;

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

    // 比如我有这样一个需求,首先注册一个用户,然后自动给用户发一个帖子,包含两个增加的操作,认为这是一个完整的业务要保证它的事务性
    // 直接写@Transactional会选择一个默认的隔离方式,如果想手动指定需要加参数
    // 不管是哪个事务隔离机制都实现了ACID四个特性为前提
    // propagation = Propagation.REQUIRED参数解析
    // 传播机制是什么? 就比如我在写这个方法的时候会调用另外一个组件的业务方法,就是业务方法a可能会调用业务方法b,同时两个业务方法都有可能配置了事务管理,以谁为准呢?解决两个事务交叉在一起的问题
    // REQUIRED: 支持当前事务(外部事务),如果不存在则创建新事务.(A调用B,如果A业务调用了我,A有事务就按照他的来,A没有就调用我的事务)
    // REQUIRES_NEW: 创建一个新事务,并且暂停当前事务(外部事务).(A调B,A无视B的事务,我永远都创建一个新事务,按自己的方式执行)
    // NESTED: 如果当前存在事务(外部事务),则嵌套在该事务中执行(独立的提交和回滚),否则就会REQUIRED一样.(A调用B,A有事务那我就嵌套在A里执行我自己的事务,我有自己独立的提交和回滚,否则A(外部事物)如果没有事务,那就和Required一样)
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED) // 读取已提交的隔离机制,传播方式propagation
    public Object save1() {
        // 新增用户
        User user = new User();
        user.setUsername("alpha");
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
        user.setEmail("alpha@qq.com");
        user.setHeaderUrl("http://image.nowcoder.com/head/99t.png");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 新增帖子
        DiscussPost post = new DiscussPost();
        // user.getId()是可以取到id的,一方面因为insert语句用的是明确指定插入列的列表的写法,同时没有指定id,所以MySQL会自动生成id
        // 同时Mybatis的xml对应的sql中指定了keyProperty="id",mybatis会自动回填id到参数user中
        post.setUserId(user.getId());
        post.setTitle("Hello");
        post.setContent("新人报道!");
        post.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(post);

        // 这个肯定会报错
        // 但是这样写数据依然会插入进去,因为此时没有配置事务,所以每次执行完DML(Data Manipulation Language): 数据操作语言语句之后,马上就会commit生效,不会回滚
        Integer.valueOf("abc");

        return "ok";
    }

    public Object save2() {
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        return transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                // 新增用户
                User user = new User();
                user.setUsername("beta");
                user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
                user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
                user.setEmail("beta@qq.com");
                user.setHeaderUrl("http://image.nowcoder.com/head/999t.png");
                user.setCreateTime(new Date());
                userMapper.insertUser(user);

                // 新增帖子
                DiscussPost post = new DiscussPost();
                post.setUserId(user.getId());
                post.setTitle("你好");
                post.setContent("我是新人!");
                post.setCreateTime(new Date());
                discussPostMapper.insertDiscussPost(post);

                Integer.valueOf("abc");

                return "ok";
            }
        });
    }
}
