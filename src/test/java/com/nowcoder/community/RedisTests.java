package com.nowcoder.community;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
//通过下面注解实现在测试中也能引用CommunityApplication配置类,这样才接近正式环境,一会运行时的测试代码就是以CommunityApplication为配置类了
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTests {

    @Autowired
    private RedisTemplate redisTemplate;

    // 下面所有的opsFor什么什么的ops,实际都是operations的缩写

    @Test
    public void testStrings(){

        String redisKey = "test:count";

        redisTemplate.opsForValue().set(redisKey,1);

        System.out.println("redisTemplate = " + redisTemplate.opsForValue().get(redisKey));
        System.out.println("redisTemplate = " + redisTemplate.opsForValue().increment(redisKey));
        System.out.println("redisTemplate = " + redisTemplate.opsForValue().decrement(redisKey));
    }

    @Test
    public void testHash(){
        String redisKey = "test:hash";

        redisTemplate.opsForHash().put(redisKey,"id",1);
        redisTemplate.opsForHash().put(redisKey,"username","zhangsan");
        System.out.println(redisTemplate.opsForHash().get(redisKey,"id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey,"username"));

    }

    @Test
    public void testList(){

        String redisKey = "test:list";

        redisTemplate.opsForList().leftPush(redisKey,101);
        redisTemplate.opsForList().leftPush(redisKey,102);
        redisTemplate.opsForList().leftPush(redisKey,103);

        System.out.println(redisTemplate.opsForList().size(redisKey));
        System.out.println(redisTemplate.opsForList().index(redisKey,0));
        System.out.println(redisTemplate.opsForList().range(redisKey,0,2));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
    }

    @Test
    public void testSet(){

        String redisKey = "test:set";
        redisTemplate.opsForSet().add(redisKey,"刘备","关羽","张飞");

        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        System.out.println(redisTemplate.opsForSet().members(redisKey));
    }

    @Test
    public void testSortedSet(){

        String redisKey = "test:SortedSet";

        redisTemplate.opsForZSet().add(redisKey,"唐僧",80);
        redisTemplate.opsForZSet().add(redisKey,"孙悟空",90);
        redisTemplate.opsForZSet().add(redisKey,"八戒",100);

        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));
        System.out.println(redisTemplate.opsForZSet().score(redisKey,"八戒"));

        // 返回的是索引
        System.out.println(redisTemplate.opsForZSet().rank(redisKey,"八戒"));
        System.out.println(redisTemplate.opsForZSet().range(redisKey,0,2));
    }

    @Test
    public void testKey(){
        redisTemplate.delete("test:hash");

        System.out.println(redisTemplate.hasKey("test:list"));
        System.out.println(redisTemplate.hasKey("test:hash"));

        redisTemplate.expire("test:SortedSort",10, TimeUnit.SECONDS);

    }

    // 在创建redis访问对象的时候就将key绑定进去,这样就不用每次添加删除等操作都要讲redisKey传入
    // 可以产生一个访问key的对象
    // 将一个key绑定到一个对象上就叫绑定对象BoundOperations
    @Test
    public void testBoundOperations(){

        String redisKey = "test:count";
        BoundValueOperations boundValueOperations = redisTemplate.boundValueOps(redisKey);

        // 创建好BoundValueOperations后,他和redisTemplate.opsForValue()的api是相似的,只不过不用传key了

        boundValueOperations.increment();
        boundValueOperations.increment();
        boundValueOperations.increment();

        System.out.println(boundValueOperations.get());

    }

    // 编程式事务
    @Test
    public void testTransactional(){
        // 处理事务
        // 方法内部需要传一个接口的实例SessionCallback,这个接口会自带一个execute方法,
        // 等执行时,底层会去调用execute方法,同时也会把执行对象RedisOperations传入进来
        // 最后会返回一些数据给上层redisTemplate.execute()方法
        Object object = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String redisKey = "test:tx";
                // redisOperations.multi()启用事务
                redisOperations.multi();

                redisOperations.opsForSet().add(redisKey,"张三");
                redisOperations.opsForSet().add(redisKey,"李四");
                redisOperations.opsForSet().add(redisKey,"王五");

                // 测试在Redis事务中能否查询
                System.out.println(redisOperations.opsForSet().members(redisKey));

                // redisOperations.exec()提交事务
                return redisOperations.exec();
            }
        });
        System.out.println("object = " + object);

    }

}
