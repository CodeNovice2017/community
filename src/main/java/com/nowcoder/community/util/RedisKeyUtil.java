package com.nowcoder.community.util;

public class RedisKeyUtil {

    // 项目中的key是由:连接的,有些单词可能是固定的,有些可能是需要传入的
    private static final String SPLIT = ":";

    // 现在要存帖子或者评论的赞,帖子和评论可以一起存,帖子和评论统称为实体,要存实体的赞
    // 希望实体的赞的KEY以某一个常量为开头
    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    // 写一个静态方法,要求传入一些变量,我来拼接为完整的KEY
    // 生成某个实体的赞
    // 实体的类型和实体的id传入
    // 为什么存set就是如果未来我们的需求变了,我想看到到底谁给我赞了,那么只存一个整数就满足不了我们的需求了,
    // 集合中装userId,当需要统计赞的数量的时候,就直接统计value的个数(userId的个数),如果未来想知道谁点的赞,也可以从userId获取的到
    // like:entity:entityType:entityId -> set
    public static String getEntityLikeKey(int entityType, int entityId){
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }
}
