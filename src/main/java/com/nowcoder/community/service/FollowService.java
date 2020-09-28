package com.nowcoder.community.service;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowService implements CommunityConstant {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    // 关注
    public void follow(int userId,int entityType,int entityId){

        // 我们在关注的时候,要存两份数据,一个是关注的目标,一个是目标的粉丝,所以说要保证事务
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {

                String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                redisOperations.multi();

                redisOperations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                redisOperations.opsForZSet().add(followerKey,userId,System.currentTimeMillis());

                return redisOperations.exec();
            }
        });
    }

    // 取消关注
    public void unfollow(int userId,int entityType,int entityId){

        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {

                String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                redisOperations.multi();

                redisOperations.opsForZSet().remove(followeeKey, entityId);
                redisOperations.opsForZSet().remove(followerKey,userId);

                return redisOperations.exec();
            }
        });
    }

    // 查询某个用户关注的实体的数量
    public long findFolloweeCount(int userId,int entityType){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);

    }

    // 查询某个实体粉丝的数量
    public long findFollowerCount(int entityType,int entityId){
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    // 查询当前用户有没有关注当前目标目标
    public boolean hasFollowing(int userId,int entityType,int entityId){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);

        // 有很多方法都可以来判断
        // 比如查询一下分数,查询一下某个数据的分数,能查到就说明有,查不到就没有
        return redisTemplate.opsForZSet().score(followeeKey,entityId) != null;
    }

    // 目前是写的只针对用户关注的查询,实际上可以写成目标实体的查询,但是那样就是在一个方法中要做entityType的判断,目前只写死为用户的查询即可,要明白此处以后可以扩展
    // 查询某个用户关注的人
    // 集合里面是关注的人或者是粉丝的id,但传给页面的肯定不能只是id,需要有user,还需要有关注的时间,所以其实是一个User对象和时间整合在一起的
    public List<Map<String,Object>> findFollowees(int userId,int offset,int limit){

        String followeeKey = RedisKeyUtil.getFolloweeKey(userId,ENTITY_TYPE_USER);
        // 按reverseRange返回按分数(时间)倒叙排列的关注用户
        // 因为分页的需求,我们每次只查offset到offset+limit-1范围的关注者value,
        // 因为redis不像mysql是传入offset和limit,redis是传入范围的两端,而且是左闭右闭(我猜测)
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey,offset,offset+limit-1);
        if(targetIds == null){
            return null;
        }
        List<Map<String,Object>> list = new ArrayList<>();
        for (Integer targetId:
             targetIds) {
            Map<String,Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user",user);
            // 当时点关注的分数是当时时间的毫秒数,要转换为date格式
            Double score = redisTemplate.opsForZSet().score(followeeKey,targetId);
            map.put("followTime",new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }

    // 查询某个用户的粉丝
    public List<Map<String,Object>> findFollowers(int userId,int offset,int limit){
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);

        // Spring整合Redis,使用有序集合时,调用range或者reverseRange查到一个范围内的数据会做一个排序,返回的是一个有序Set对象
        // 但是Set在Java默认的实现是无序集合,为什么呢?,range或者reverseRange返回的是Set接口不假,但是它的实现类实际是它内置的,它自己实现了一个有序的set集合
        Set<Integer> followerIds = redisTemplate.opsForZSet().reverseRange(followerKey,offset,offset+limit-1);
        if(followerIds == null){
            return null;
        }
        List<Map<String,Object>> list = new ArrayList<>();
        for (Integer followerId:
                followerIds) {
            Map<String,Object> map = new HashMap<>();
            User user = userService.findUserById(followerId);
            map.put("user",user);
            // 当时点关注的分数是当时时间的毫秒数,要转换为date格式
            Double score = redisTemplate.opsForZSet().score(followerKey,followerId);
            map.put("followTime",new Date(score.longValue()));
            list.add(map);
        }

        return list;
    }
}
