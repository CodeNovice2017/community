package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    // 分页查询,那么返回的肯定是一堆帖子
    // 首页上查询时实际是不用userId,此时加上userId,是为了将来可能会涉及一个我的帖子
    // 可以编写动态的SQL,userId为0时,不需要加上userId这个条件,而当userId不为0时,才要这个条件
    // offset 是每一页起始行的行号,limit每页最多显示帖子数
    // 因为要支持分页,那就要求要知道一共需要有多少页,帖子数/每页显示多少条
    List<DiscussPost> selectDiscussPosts(int userId, int offset,int limit);


    // @Param注解用于给参数取别名,比如有的参数名比较长,嫌到sql中写的麻烦就起一个别名
    // 如果只有一个参数,并且在<if>里使用,则必须加别名,如果需要动态的拼一个条件,并且这个方法有且只有一个条件,这个时候这个参数之前就必须要取别名
    int selectDiscussPostRows(@Param("userId") int userId);
}

