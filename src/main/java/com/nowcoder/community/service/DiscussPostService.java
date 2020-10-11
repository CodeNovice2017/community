package com.nowcoder.community.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.DiscussPostAndUserName;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostService {

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    // 本地缓存Caffeine的自定义参数
    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    // Caffeine核心接口: Cache, LoadingCache, AsyncLoadingCache

    // 声明两个缓存
    // 之后还要初始化,那么什么时候初始化呢? 这个缓存不需要初始化多次,一般是在服务启动或者首次调用Service的时候初始化一次即可
    // 那么我们可以给当前的Service增加一个初始化方法,这个类的初始化方法唯一调用一次的时候我初始化这两个缓存
    // 帖子列表的缓存
    // 为什么LoadingCache需要两个值呢?因为其实所有的缓存都是一样的,都是按照Key缓存Value
    private LoadingCache<String,List<DiscussPost>> postListCache;
    // 帖子总数的缓存
    private LoadingCache<Integer,Integer> postRowsCache;

    // 上面解释了什么时候初始化两个缓存
    @PostConstruct
    public void init(){
        // 初始化帖子列表缓存
        // 初始化的方式是固定的
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                // 当把缓存写入Caffeine之后,多长时间过期
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                // build()方法使参数生效,同时返回LoadingCache对象,同时build需要一个参,这个参数是个接口,需要匿名实现
                // 这个接口是干嘛的呢?就是说当我尝试从缓存中取数据的时候,Caffeine会看一下有没有数据,有得话给你返回,
                // 没有的话,它需要知道怎么去查这个数据,然后偶把这个数据更新到缓存里,也就是提供一个查询数据库得到初始化数据的办法
                // 这个覆写方法load的参数就是我们传入的key
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Override
                    public @Nullable List<DiscussPost> load(@NonNull String s) throws Exception {
                        if (s == null || s.length() == 0) {
                            throw new IllegalArgumentException("参数错误!");
                        }

                        // 判断这个key对不对,因为按照key的设定应该是 offset:limit的形式的key
                        String[] params = s.split(":");
                        if (params == null || params.length != 2) {
                            throw new IllegalArgumentException("参数错误!");
                        }
                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);

                        // 二级缓存添加的位置,在这里可以再访问Redis,添加一级缓存

                        logger.debug("load posts from DB.");
                        // 缓存只适用于userId == 0 && orderMode == 1这一个场景
                        return discussPostMapper.selectDiscussPosts(0,offset,limit,1);
                    }
                });
        // 初始化帖子总数缓存
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Nullable
                    @Override
                    public Integer load(@NonNull Integer key) throws Exception {
                        logger.debug("load postRows from DB.");
                        return discussPostMapper.selectDiscussPostRows(key);
                    }
                });
    }
    // 这个缓存什么时候会调用呢?
    // 一方面findDiscussPost(int userId, int offset, int limit,int orderMode)是多种场景都会调用
    // 访问首页,按照默认时间方式排序会调用,按照热门帖子的方式会调用,查看自己发布帖子的功能调用
    // 那么这个帖子列表的缓存的Key应该怎么配置呢?
    // 但我只想缓存的是热门帖子,也就是orderMode==1时,并且是缓存首页的热门帖子点击,而不是用户自己的帖子,所以userId是不传的
    // 所以缓存的条件是userId==0,orderMode==1,而我缓存的是一页数据,这一页和int offset, int limit有关,这两个条件唯一确定一页
    // 所以这个key应该是int offset, int limit的组合

    // 当前的方法实际上就是吧数据查出来,没有什么业务,有可能就有想法直接略过Service这层,直接在Controller内调用Mapper,但是这是不对的,为了将来业务考虑也不应该直接略过Service层

    public List<DiscussPost> findDiscussPost(int userId, int offset, int limit,int orderMode){
        // 不要有疑惑,如果缓存没有数据怎么办?这个条件添加的意思是,只要是热门帖子列表那我就是去缓存中查询数据,就算查不到
        // 我也会通过之前postListCache的初始化里声明了查不到数据应该如何去数据库中查找,然后更新数据到缓存中再返回
        // 所以无论缓存有无数据,我都是在这个方法内完成查询的
        if(userId == 0 && orderMode == 1){
            return postListCache.get(offset+":"+limit);
        }
        // 记个日志,方便一会测试的时候看到底这个数据是访问的数据库还是访问的缓存
        logger.debug("load posts from DB.");
        return discussPostMapper.selectDiscussPosts(userId,offset,limit,orderMode);
    }

    // 访问帖子列表总会调用这个方法,比较频繁,所以可以缓存
    // 为什么可以缓存呢?因为这个总行数实际决定总页数,那么总页数稍微少一页对用户的影响不大
    public int findDiscussPostRows(int userId) {
        if(userId == 0){
            // 其实我们这个帖子行数缓存不需要key,因为就一条数据,但还是必须有个key,所以就用userId=0作为key了,就是用0做key了
            return postRowsCache.get(userId);
        }
        logger.debug("load postRows from DB.");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    // 还有一个问题是我们查询到的这个结果,他有一个外键userId,但是在页面显示的时候要显示的是user的名称,而不是userId,
    // 有两种解决办法,1是写sql都时候,关联查询用户,把用户的数据也一起查到,2是单独的查到user,然后把查到的user对象和discussPost一起返回
    // 建议选择第二种,虽然现在麻烦一些,但是之后用redis缓存数据的时候就会快很多

    // 添加帖子业务
    public int addDiscussPost(DiscussPost discussPost){
        if(discussPost == null){
            throw new IllegalArgumentException("参数不能为空!");
        }
        // title和content是需要做敏感词过滤的,同时最好把<script></script>这种标签也给去掉,我们只希望浏览器认为这是普通的文字,而不希望认为这是html标签
        // 转义html标记,利用Spring MVC自带的一个工具即可
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));

        // 过滤敏感词
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));

        return discussPostMapper.insertDiscussPost(discussPost);
    }

    // 查询帖子
    public DiscussPost findDiscussPostById(int id){
        return discussPostMapper.selectDiscussPostById(id);
    }

    // 查询帖子另一种方法(self)
    public DiscussPostAndUserName findDiscussPostAndUserNameById(int id){
        return discussPostMapper.selectDiscussPostAndUserNameById(id);
    }

    // 在帖子的业务组件加一个更新comment_count评论数的方法
    // 因为毕竟comment_count是discuss_post表的列
    // 然后在CommentService编写评论业务的时候,加上这个业务组件
    // 也就是说Service不止可以依赖自己的Mapper,也可以依赖其他的Service
    public int updateCommentCount(int id,int commentCount){
        return discussPostMapper.updateCommentCount(id,commentCount);
    }

    public int updateType(int id, int type) {
        return discussPostMapper.updateType(id, type);
    }

    public int updateStatus(int id, int status) {
        return discussPostMapper.updateStatus(id, status);
    }

    public int updateScore(int id, double score) {
        return discussPostMapper.updateScore(id, score);
    }


}
