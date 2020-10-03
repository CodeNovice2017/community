package com.nowcoder.community.entity;

import java.util.HashMap;
import java.util.Map;

public class Event {
    // kafka角度叫事件的主题,从业务角度实际是事件的类型(消息,点赞,关注)
    private String topic;

    // 简单解析一下下面的事件,张三给李四的帖子点赞
    // 这个userId就是张三,事件的触发者
    // entityType=1 实体类型为1是代表帖子相关的实体
    // entityId=帖子ID
    // entityUserId=李四的用户Id,代表的是实体的作者Id,
    // 如果是关注某人,那就是关注的人的userId,如果是点赞了帖子,那就是,帖子作者的userId,如果是发送了消息,那就是消息接受者的userId

    // 事件是谁发出,事件触发的人
    private int userId;

    // 这个事件是发生在哪个实体上呢? 是这个人点赞了还是关注了呢?
    private int entityType;
    private int entityId;

    // 这个实体的作者是谁,
    private int entityUserId;

    // 我们事件应该有一定的通用性,所以未来如果有其他的事件,可能还会有一些特殊的数据进行记录
    private Map<String,Object> data = new HashMap<>();


    // 为了构造对象方便,对get/set方法进行一定的改造

    public String getTopic() {
        return topic;
    }

    // set方法原本没有返回值,我现在加上就返回它自己
    // 这样做的好处是,当我们调用了set方法的时候,我设置了topic,那么我肯定还要设置其他的属性,
    // 那么我setTopic()以后又返回当前对象,那么我们又可以直接setTopic().setXX()其他的方法
    // 那就会又有新的问题,那为什么不是直接用一个包含所有属性的构造器呢?
    // 这样也可以,但是问题是我们的属性很多,如果用一个很多参数的构造器,其实也很麻烦,
    // 而且有的时候,某一个字段可能不需要传,但是构造器又声明了,就不得不传null等
    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;

    }

    public int getEntityUserId() {
        return entityUserId;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    // setData也做类似的处理,但是我不想让外界传入时,直接传一个map,而是传入一个key,和value
    // 而且如果有很多的key value就一直setData()即可
    public Event setData(String key,Object value) {
        this.data.put(key,value);
        return this;
    }
}
