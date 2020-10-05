package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
//通过下面注解实现在测试中也能引用CommunityApplication配置类,这样才接近正式环境,一会运行时的测试代码就是以CommunityApplication为配置类了
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchTests {
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private DiscussPostRepository discussPostRepository;
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    // 如何向ES服务器添加数据
    @Test
    public void testInsert(){
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
    }
    @Test
    public void testInsertList(){
        for (int i = 101; i < 104; i++) {
            discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(i,0,100));
        }
        for (int i = 111; i < 113; i++) {
            discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(i,0,100));
        }
        for (int i = 131; i < 135; i++) {
            discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(i,0,100));
        }

    }
    @Test
    public void testUpdate(){
        DiscussPost discussPost = discussPostMapper.selectDiscussPostById(231);
        discussPost.setContent("我是新人,使劲灌水");
        discussPostRepository.save(discussPost);
    }
    @Test
    public void testDelete(){
        // 直接将索引里的数据全部删了
        discussPostRepository.deleteAll();
    }
    @Test
    public void testSearchRepository(){
        // 构造搜索的条件
        // 不只是条件,还有搜完之后要不要排序,要不要分页等
        // 搜索结果要不要高亮显示
        // 比如搜的时候,搜索条件是互联网
        // 返回的是互联网求职暖春计划,就要把匹配的词变红,es可以把匹配到的词前后加上一个标签
        // 这个标签是由我们自己指定的,比如我们制定<em></em>然后返回网页显示时,我们通过页面的css设置em标签为红色
        // 所以Elasticsearch还有能力将返回的结果当中匹配到的关键词做高亮显示,实现机制就是上面说的
        // 所以说构造查询的条件是工作的首要

        // Spring实现了一个SearchQuery的实现类NativeSearchQuery,
        // 也提供了NativeSearchQueryBuilder这个工具类用于构建一个NativeSearchQuery
        // 用这个比较方便,可以不断的调用NativeSearchQueryBuilder的withQuery()方法
        // withQuery()用于构建搜索条件,返回的还是这个NativeSearchQueryBuilder本身,搜索条件利用另外一个对象去构造QueryBuilder
        // 比如我希望搜的是互联网寒冬,我希望即从title里搜,也从content里搜
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬","title","content"))
                // 按照type->score->createTime来排序,type意味是否置顶,status意味是否加精但是会被计算进score中,score意味着帖子的价值,最后按照createTime来拍
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                // 然后不可能直接查,因为有可能几万条数据,要按分页查询
                // 用PageRequest构建分页的条件,of()方法的第一个条件是第几页,第二是这一页最多显示多少条数据
                .withPageable(PageRequest.of(0,10))
                // 指定哪些字段进行高亮显示,并且指定加什么标签实现高亮显示
                // 每个字段可以通过new HighlightBuilder.Field()指定,preTags指定前置标签
                .withHighlightFields(new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>")
                        ,new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>"))
                // 最后通过.build()方法返回接口的实现类
                .build();
        // 返回的是分页的数据,分页的数据是利用分页对象Page进行封装的,这个Page不是我们自己写的page,是人家的Page对象
        // Page中有多个实体,可以把这个Page看做一个集合,里面封装了多个实体类
        Page<DiscussPost> page = discussPostRepository.search(searchQuery);
        System.out.println("page.getTotalElements() = " + page.getTotalElements());
        System.out.println("page.getTotalPages() = " + page.getTotalPages());
        // 当前处于第几页
        System.out.println("page.getNumber() = " + page.getNumber());
        // 每一页最多显示几条数据
        System.out.println("page.getSize() = " + page.getSize());
        // 这个page是可以直接遍历的,应该是实现了Iterable接口,就可以被遍历
        // 下面这个es Page遍历出的结果是原始的结果,
        // 确实ElasticsearchRepository的实现类确实在查询结果时把ES带有标签的高亮内容已经返回了
        // 但是它没有把这个内容合入结果里面去,也就是ES返回的数据有两份,一份是原始结果,一份是带高亮标签结果,
        // 我们需要把高亮标签的数据放原始数据里做一个替换才可以
        // 具体discussPostRepository的search方法的底层是调用了elasticTemplate.queryForPage(searchQuery,class,SearchResultMapper)
        // 查到的数据由SearchResultMapper做了一个处理,如果要把两份数据组装在一起,就要利用这个Mapper去处理,但是discussPostRepository.search()没有利用去处理
        // 底层获取到了高亮显示的值,但是没有返回,要么去重写方法,要么就干脆不用ElasticsearchRepository实现类的search方法,而是直接用elasticTemplate的方法
        for (DiscussPost post : page){
            System.out.println("post = " + post);
        }
    }
    @Test
    public void testElasticsearchTemplate(){
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬","title","content"))
                // 按照type->score->createTime来排序,type意味是否置顶,status意味是否加精但是会被计算进score中,score意味着帖子的价值,最后按照createTime来拍
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                // 然后不可能直接查,因为有可能几万条数据,要按分页查询
                // 用PageRequest构建分页的条件,of()方法的第一个条件是第几页,第二是这一页最多显示多少条数据
                .withPageable(PageRequest.of(0,10))
                // 指定哪些字段进行高亮显示,并且指定加什么标签实现高亮显示
                // 每个字段可以通过new HighlightBuilder.Field()指定,preTags指定前置标签
                .withHighlightFields(new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>")
                                    ,new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>"))
                // 最后通过.build()方法返回接口的实现类
                .build();
        // 自己实现SearchResultMapper这个接口,直接new一个匿名类实现接口
        // 这样就是queryForPage这个方法执行完会得到结果,它得到的结果会自动交给Mapper去处理,然后从Mapper把结果做一个封装返回就可以了
        // 也就是说这个SearchResultMapper实际就是一个Mapper的功能,将ES数据库的数据映射为相应的对象的各个属性上
        Page<DiscussPost> page = elasticsearchTemplate.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                // 先通过searchResponse获取他这次搜索命中的数据
                SearchHits searchHits = searchResponse.getHits();
                // 判断如果命中小于等于0,那就是没查到,直接返回
                if(searchHits.getTotalHits()<=0){
                    return null;
                }
                // 从searchHits遍历命中的数据,然后把数据找到,自己做处理,然后放进这个集合里
                List<DiscussPost> list = new ArrayList<>();
                for (SearchHit hit: searchHits
                     ) {
                    DiscussPost post = new DiscussPost();
                    // 命中数据的形式我们在postman看到过了,返回的是JSON格式的数据,在这个SearchHit对象里是把JSON格式的数据封装为了Map
                    // 所以从hit里面能调用hit.getSourceAsMap()获得Map进而得到每一个字段的值
                    String id = hit.getSourceAsMap().get("id").toString();
                    post.setId(Integer.valueOf(id));

                    String userId = hit.getSourceAsMap().get("userId").toString();
                    post.setUserId(Integer.valueOf(userId));

                    // 高亮显示的title依然需要单独的方式获取,不能直接获取高亮显示的title放入这里,
                    // 因为有可能当前返回的title,没有匹配的关键字,那个关键字在content中,
                    // 所以我们需要把原始的title和content先取到放入post中去,如果之后判断高亮显示的字符,有就覆盖
                    String title = hit.getSourceAsMap().get("title").toString();
                    post.setTitle(title);

                    String content = hit.getSourceAsMap().get("content").toString();
                    post.setContent(content);

                    String status = hit.getSourceAsMap().get("status").toString();
                    post.setStatus(Integer.valueOf(status));

                    // createTime这个字符串是一个long类型的字符串,他不是年月日的那种形式
                    // 也就是ES在存日期类型的时候,是把它转换为long整数类型的字符串来存的
                    String createTime = hit.getSourceAsMap().get("createTime").toString();
                    post.setCreateTime(new Date(Long.valueOf(createTime)));

                    String commentCount = hit.getSourceAsMap().get("commentCount").toString();
                    post.setCommentCount(Integer.valueOf(commentCount));

                    // 处理高亮显示的结果
                    HighlightField titleField = hit.getHighlightFields().get("title");
                    if (titleField != null) {
                        // getFragments()返回的是一个数组,因为title中匹配的词条,有可能是多个,比如匹配的是互联网寒冬
                        // 有可能匹配的既有互联网,又有寒冬,甚至有可能包含两个互联网这个次,如果匹配多段的话,咱们只要第一段就行了,就不每段都显示了
                        post.setTitle(titleField.getFragments()[0].toString());
                    }

                    HighlightField contentField = hit.getHighlightFields().get("content");
                    if (contentField != null) {
                        post.setContent(contentField.getFragments()[0].toString());
                    }

                    list.add(post);
                }
                // 返回的类型是AggregatedPage这样一个接口,所以我们需要返回一个这个接口的实现类
                // 要传好多的参数,参数的顺序可以查看Repository的源码
                return new AggregatedPageImpl(list, pageable, searchHits.getTotalHits(), searchResponse.getAggregations(), searchResponse.getScrollId(), searchHits.getMaxScore());
            }
        });
        System.out.println("page.getTotalElements() = " + page.getTotalElements());
        System.out.println("page.getTotalPages() = " + page.getTotalPages());
        // 当前处于第几页
        System.out.println("page.getNumber() = " + page.getNumber());
        // 每一页最多显示几条数据
        System.out.println("page.getSize() = " + page.getSize());
        // 这个page是可以直接遍历的,应该是实现了Iterable接口,就可以被遍历
        // 下面这个es Page遍历出的结果是原始的结果,
        // 确实ElasticsearchRepository的实现类确实在查询结果时把ES带有标签的高亮内容已经返回了
        // 但是它没有把这个内容合入结果里面去,也就是ES返回的数据有两份,一份是原始结果,一份是带高亮标签结果,
        // 我们需要把高亮标签的数据放原始数据里做一个替换才可以
        // 具体discussPostRepository的search方法的底层是调用了elasticTemplate.queryForPage(searchQuery,class,SearchResultMapper)
        // 查到的数据由SearchResultMapper做了一个处理,如果要把两份数据组装在一起,就要利用这个Mapper去处理,但是discussPostRepository.search()没有利用去处理
        // 底层获取到了高亮显示的值,但是没有返回,要么去重写方法,要么就干脆不用ElasticsearchRepository实现类的search方法,而是直接用elasticTemplate的方法
        for (DiscussPost post : page){
            System.out.println("post = " + post);
        }
    }
}
