package com.nowcoder.community.quartz;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.ElasticsearchService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DiscussPostScoreRefreshJob implements Job, CommunityConstant {

    // 定时任务启动的时候,最好在关键的节点记录日志,这样将来出了问题,中断,可以查到位置
    private static final Logger logger = LoggerFactory.getLogger(DiscussPostScoreRefreshJob.class);

    // 计算的数据来源与Redis
    @Autowired
    private RedisTemplate redisTemplate;

    // 计算过程中还需要查帖子,查帖子点赞数量等,还要把数据同步到数据库和搜索引擎里,因为帖子的数据变了,搜索引擎也要变
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private ElasticsearchService elasticsearchService;

    // 牛客纪元时间常量
    private static final Date epoch;
    // 静态块是因为final类型的常量要预先初始化
    static{
        // SimpleDateFormat能把日期转为字符串,也能把字符串解析为日期,不过前提要指定匹配的格式
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (Exception e) {
            throw new RuntimeException("初始化牛客纪元失败!",e);
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreKey();
        // 因为每一个Key都要算一下,反复做这项操作,所以要用BoundSetOperations
        // 提供了对key的“bound”(绑定)便捷化操作API，可以通过bound封装指定的key，
        // 然后进行一系列的操作而无须“显式”的再次指定Key，即BoundKeyOperations
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        // 要先看redis空间有没有值
        // 比如半夜的时候,没有人访问社区
        if(operations.size() == 0){
            logger.info("[任务取消] 没有需要刷新的帖子!");
            return;
        }
        // 开始和结束都添加日志信息,方便以后查看可能某次任务特别的慢,容易定位到
        logger.info("[任务开始] 正在刷新帖子分数ing 待刷新的帖子个数有:" + operations.size());
        // 只要redis里有数据就计算
        while(operations.size() > 0){
            // 每次算都调用refresh方法刷新分数,
            // 然后需要把帖子id传入,因为operations是一个集合,集合有一个方法是弹出一个值,每次弹出一个值就减少一个
            this.refresh((Integer)operations.pop());
        }
        logger.info("[任务结束] 帖子分数刷新完毕!");
    }
    private void refresh(int postId){
        DiscussPost discussPost = discussPostService.findDiscussPostById(postId);
        // 万一有这个帖子有人点了赞,要算分数的,但是算之前被管理员给删除了,discussPostService.findDiscussPostById那么就会查不到
        if(discussPost == null){
            logger.error("该帖子不存在: id = " + postId);
            return;
        }
        // 正式计算分数
        // 是否加精
        Boolean wonderful = discussPost.getStatus() == 1;
        // 评论数量
        int commentCount = discussPost.getCommentCount();
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,postId);

        // log(精华分+评论数*10+点赞数*2+收藏数*2) + (发布时间-牛客纪元)
        // (精华分+评论数*10+点赞数*2+收藏数*2)通常称为权重

        // 计算权重
        double w = (wonderful ? 75 : 0) + commentCount*10 + likeCount*2;
        // 分数计算 = 帖子权重 + 距离天数
        // Math.max(w,1),返回w和1较大的数,以免帖子分数出现负分,最低都让其为0
        // (discussPost.getCreateTime().getTime() - epoch.getTime()) Date转为ms相减
        double score = Math.log10(Math.max(w,1)) + ((discussPost.getCreateTime().getTime() - epoch.getTime())/(1000*3600*24));
        // 更新帖子分数
        discussPostService.updateScore(postId,score);
        // 同步搜索的数据
        // 要先给实体设置新的分数,否则传入Elasticsearch的还是旧的discussPost实体
        discussPost.setScore(score);
        elasticsearchService.saveDiscussPost(discussPost);
    }
}
