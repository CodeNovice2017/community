package com.nowcoder.community.service;


import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    // 点赞的业务方法
    // 不需要返回值,只要不报错就是点赞成功了
    // 点赞者是谁,点赞的实体是哪个
    public void like(int userId,int entityType,int entityId,int entityUserId){

        // 4.3 重构之前的like
//        // 拼出KEY
//        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
//
//        // 第一次点赞是赞,第二次点赞是取消赞,所以需要判断当前用户点没点过赞
//        boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey,userId);
//        if(isMember){
//            redisTemplate.opsForSet().remove(entityLikeKey,userId);
//        }else{
//            redisTemplate.opsForSet().add(entityLikeKey,userId);
//        }
        // 4.4 重构之后的like
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);

                // 注意这个userId可不是参数传入的userId,这个userId是点赞者的userId,而我这个要创建的userLikeKey是被点赞者的点赞数量统计的key
                // 即应该是这个实体的拥有者,帖子或者评论的作者
                // 那么我们就多加一个参数entityUserId,因为在点赞的时候,是在帖子页面的,那么作者的userId是很容易传入的
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);

                // 看当前用户有没有对实体点过赞
                // 注意查询一定要放在事务的范围之外
                boolean isMember = redisOperations.opsForSet().isMember(entityLikeKey,userId);

                redisOperations.multi();

                if(isMember){
                    // 对entityLikeKey做处理
                    redisOperations.opsForSet().remove(entityLikeKey,userId);
                    // 对userLikeKey做处理
                    redisOperations.opsForValue().decrement(userLikeKey);
                }else{
                    // 对entityLikeKey做处理
                    redisOperations.opsForSet().add(entityLikeKey,userId);
                    // 对userLikeKey做处理
                    redisOperations.opsForValue().increment(userLikeKey);
                }
                return redisOperations.exec();
            }
        });
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


    // 查询某个用户获得赞的数量
    public int findUserLikeCount(int userId){
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);

        Integer count = (Integer)redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count.intValue();
    }

}
