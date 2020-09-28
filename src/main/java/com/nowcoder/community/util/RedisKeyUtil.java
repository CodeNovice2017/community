package com.nowcoder.community.util;

public class RedisKeyUtil {

    // 项目中的key是由:连接的,有些单词可能是固定的,有些可能是需要传入的
    private static final String SPLIT = ":";

    // 现在要存帖子或者评论的赞,帖子和评论可以一起存,帖子和评论统称为实体,要存实体的赞
    // 希望实体的赞的KEY以某一个常量为开头
    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    private static final String PREFIX_USER_LIKE = "like:user";

    // 关注Key
    // 声明两个前缀意味着我们要存两份数据,和点赞一样,一方面为了存业务相关的数据,另一方面是为了统计方便
    // 我关注了某人,那我就把我的目标存下来
    // 我关注的那个人,他就是被关注者,我就是他的粉丝,我以那个人为Key,把我作为他的粉丝存进去
    // 这样的话,从我的角度统计我的目标很好统计,从他的角度统计他的粉丝,他也方便,综上,所以存储两份数据
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";


    // 写一个静态方法,要求传入一些变量,我来拼接为完整的KEY
    // 生成某个实体的赞
    // 实体的类型和实体的id传入
    // 为什么存set就是如果未来我们的需求变了,我想看到到底谁给我赞了,那么只存一个整数就满足不了我们的需求了,
    // 集合中装userId,当需要统计赞的数量的时候,就直接统计value的个数(userId的个数),如果未来想知道谁点的赞,也可以从userId获取的到
    // like:entity:entityType:entityId -> set
    public static String getEntityLikeKey(int entityType, int entityId){
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    // 某个用户的赞
    // like:user:userId -> int
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    // 某个用户关注的实体(实体不一定是什么)
    // 因为是关注的目标,所以是followee,然后userId,表示是谁关注的,就先理解为某个用户都对那些实体进行了关注,userId,是每个用户关注的实体类型
    // 关注的是哪个实体entityType
    // value存一个有序的集合zset,zset中存的entityId,,并且以当前时间的整数形式作为分数
    // 用户关注某个实体,这样设计刚好体现了用户和实体的关系,并且从key上做了分类,对不同实体有不同的key,值是具体关注的东西,
    // 以当前时间作为分数,刚好可以先后顺序的查询
    // followee:userId:entityType -> zset(entityId,now)
    public static String getFolloweeKey(int userId,int entityType){
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    // 某个实体拥有的粉丝
    // entityType:entityId能够唯一的标识一个实体,注意不要和comment表的两个字段弄混淆了
    // follower:entityType:entityId -> zset(userId,now)
    public static String getFollowerKey(int entityType,int entityId){
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }
}
