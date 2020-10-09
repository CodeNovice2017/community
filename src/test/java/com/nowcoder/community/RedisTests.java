package com.nowcoder.community;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
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

    // HyperLogLog测试学习
    // 统计20万个重复数据的独立总数
    // 去重以后显然应该是10w
    // 99556是测试结果
    @Test
    public void testHerperLogLog(){
        String redisKey = "test:hll:01";

        for (int i = 0; i <= 100000; i++) {
            // 向数据里存10万个不重复数据
            redisTemplate.opsForHyperLogLog().add(redisKey,i);
        }
        for (int i = 0; i <= 100000; i++) {
            // Math.random() 0-1之间的左闭右开区间
            // Math.random()*100000 0-100000之间的左闭右开区间
            // Math.random()*100000 + 1 1-100001之间的左闭右开区间
            // (int)(Math.random()*100000 + 1) 1-100000之间的整数
            int r = (int)(Math.random()*100000 + 1);
            redisTemplate.opsForHyperLogLog().add(redisKey,r);
        }

        System.out.println(redisTemplate.opsForHyperLogLog().size(redisKey));
    }
    // HyperLogLog还可以对数据进行合并
    // 比如统计了这一个月每天的UV,有时我要的是一周的UV,那就应该把7天的数据合并在一起,本来比如说是某个用户在一天多次访问算一个UV
    // 如果是按照7天来统计的话,那就是7天之内算一个UV,HyperLogLog可以自动进行合并
    // 将3组数据合并, 再统计合并后的重复数据的独立总数.
    // 19833结果
    @Test
    public void testHyperLogLogUnion() {
        String redisKey2 = "test:hll:02";
        for (int i = 1; i <= 10000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey2, i);
        }

        String redisKey3 = "test:hll:03";
        for (int i = 5001; i <= 15000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey3, i);
        }

        String redisKey4 = "test:hll:04";
        for (int i = 10001; i <= 20000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey4, i);
        }

        String unionKey = "test:hll:union";
        redisTemplate.opsForHyperLogLog().union(unionKey, redisKey2, redisKey3, redisKey4);

        long size = redisTemplate.opsForHyperLogLog().size(unionKey);
        System.out.println(size);
    }

    // 统计一组数据的布尔值
    @Test
    public void testBitMap() {
        String redisKey = "test:bm:01";

        // 记录
        // 按位存所以要指定索引值是多少,也就是第几位,这里先是第一位,记住是从0开始
        // 没有设置的索引位置的位默认为false
        redisTemplate.opsForValue().setBit(redisKey, 1, true);
        redisTemplate.opsForValue().setBit(redisKey, 4, true);
        redisTemplate.opsForValue().setBit(redisKey, 7, true);

        // 查询
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2));

        // 注意统计和上面两个记录和查询没关系,上面两个只是学语法,统计也是
        // 统计,需要获取redis底层的连接才能做统计
        // 需要使用redisTemplate.execute代表执行一个redis命令,执行的时候需要我们传入一个回调的接口
        // 接口里面有一个方法doInRedis,这个方法有一个传入参数RedisConnection Redis连接,
        // 就是当我们调用execute()方法的时候,这个方法底层会调用RedisCallback(),调用RedisCallback()方法的时候会自动把Redis连接传进来
        // 然后我们可以做一些处理并返回一些值,这个返回结果最终会返回给execute方法
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                // 利用connection统计数值
                // 有了connection以后就调用bitCount方法,按位统计需要我们传入bit数组,redisKey是String,getBytes就是数组了
                // 会帮我们统计redisKey.getBytes()这个byte数组的1的个数,147是true,所以返回结果是3
                return connection.bitCount(redisKey.getBytes());
            }
        });

        System.out.println(obj);
    }

    // 统计3组数据的布尔值, 并对这3组数据做OR运算.
    @Test
    public void testBitMapOperation() {
        String redisKey2 = "test:bm:02";
        redisTemplate.opsForValue().setBit(redisKey2, 0, true);
        redisTemplate.opsForValue().setBit(redisKey2, 1, true);
        redisTemplate.opsForValue().setBit(redisKey2, 2, true);

        String redisKey3 = "test:bm:03";
        redisTemplate.opsForValue().setBit(redisKey3, 2, true);
        redisTemplate.opsForValue().setBit(redisKey3, 3, true);
        redisTemplate.opsForValue().setBit(redisKey3, 4, true);

        String redisKey4 = "test:bm:04";
        redisTemplate.opsForValue().setBit(redisKey4, 4, true);
        redisTemplate.opsForValue().setBit(redisKey4, 5, true);
        redisTemplate.opsForValue().setBit(redisKey4, 6, true);

        String redisKey = "test:bm:or";
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                // RedisStringCommands.BitOperation.OR声明运算符
                //
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(), redisKey2.getBytes(), redisKey3.getBytes(), redisKey4.getBytes());
                // 执行之后redisKey就存了一个新的值,统计这里面为真的数据的结果,和预期的一样是7,因为0-6 7位都是1为真
                return connection.bitCount(redisKey.getBytes());
            }
        });

        System.out.println(obj);

        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 3));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 4));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 5));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 6));

    }

}
