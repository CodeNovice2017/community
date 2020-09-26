package com.nowcoder.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    // 要把哪个对象装入Spring容器当中就返回哪个对象
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        // 我们是想用redisTemplate访问数据库,但是想让它有访问数据库的能力需要能够创建连接,
        // 而连接是由连接工厂创建的,则需要把连接工厂注入进来,注入给这个redisTemplate,它才能够访问数据库
        // 当我们定义一个bean的时候,方法上有这么一个参数RedisConnectionFactory redisConnectionFactory,那么Spring会自动把这个bean注入进来
        // RedisConnectionFactory redisConnectionFactory这个bean已经比容器装配了

        RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 配置redisTemplate主要是配序列化的方式
        // 要指定一种数据转化的方式
        // redis key-value结构的数据库

        // 设置key的序列化方式
        // 这个序列化方式在一个类里面有一个统一的定义,可以直接访问,RedisSerializer.string(),这个方法返回了一个能够序列化字符串的序列化器
        redisTemplate.setKeySerializer(RedisSerializer.string());

        // 设置普通value的序列化方式
        // value可以有普通的值,也可以有集合,列表,通常建议序列化为json,因为json格式的数据是结构化的,恢复也很好去读取识别
        redisTemplate.setValueSerializer(RedisSerializer.json());

        // 设置一个特殊的value value本身是个Hash,Hash又分为key和value
        // 设置Hash的key的序列化方式
        redisTemplate.setHashKeySerializer(RedisSerializer.string());

        // 设置Hash的value的序列化方式
        redisTemplate.setHashValueSerializer(RedisSerializer.json());

        // 为了让redisTemplate当中的参数生效,还需要调一下下面方法让其生效
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

}
