package com.nowcoder.community.dao.elasticsearch;

import com.nowcoder.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

// 因为这个接口是一个数据访问层的代码,而且ES也可以看做一个特殊的数据库,所以可以加入@Repository注解,不是Mapper,Mapper是Mybatis特有的注解
// 只要继承于Spring提供的默认的ElasticsearchRepository接口即可,不用实现任何方法
// 继承的时候需要加上泛型,然后声明好要处理的实体类是谁,以及实体类的主键是什么类型
// 父接口中已经定义好了对ES服务器的增删改查各种方法,我们直接去调用就可以了
@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost,Integer> {
}
