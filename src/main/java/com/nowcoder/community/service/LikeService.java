package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    // 点赞的业务方法
    // 不需要返回值,只要不报错就是点赞成功了
    // 点赞者是谁,点赞的实体是哪个
    public void like(int userId,int entityType,int entityId){

        // 拼出KEY
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);

        // 第一次点赞是赞,第二次点赞是取消赞,所以需要判断当前用户点没点过赞
        boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey,userId);
        if(isMember){
            redisTemplate.opsForSet().remove(entityLikeKey,userId);
        }else{
            redisTemplate.opsForSet().add(entityLikeKey,userId);
        }
    }

    // 同时还要注意,我们不止要点赞,后续还要统计点赞的数量,并且在详情页不只要统计数量,还要显示点赞的状态,如果没有赞就显示赞这个字,如果赞过了要显示已赞+数量

    // 查询某实体点赞的数量
    public long findEntityLikeCount(int entityType,int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);

    }

    // 统计实体点赞的状态,查询某人对某实体的点赞状态
    // 如果只是当前的业务,那么返回一个boolean值就可以满足需求了,
    // 但是设置为int类型,未来业务扩展的话,比如点踩,int更具备扩展性
    public int findEntityLikeStatus(int userId,int entityType,int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey,userId) ? 1 : 0; // 返回1就是点过赞,返回0就是没点赞
    }

}
