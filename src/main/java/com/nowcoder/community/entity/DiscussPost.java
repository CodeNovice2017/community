package com.nowcoder.community.entity;


import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

// 因为使用ElasticsearchRepository方案使用ES,加入注解,来实现对Spring整合ES的一些配置
// Spring整合ES的底层会自动的将我们的实体数据和ES服务器的索引进行映射,
// 指定实体映射到哪个索引上去,映射到哪个类型上去(文档以后会抛弃的概念,默认写_doc),映射的时候创建几个分片,几个副本在这里指定
// 未来如果我们调用这个API的时候,Spring检测到没有这个索引,分片等,那么他会自动根据这里的配置去创建,然后插入数据
// shards = 6,replicas = 3是根据服务器的处理能力来配置,分片分6分,分6个区域
@Document(indexName="discusspost",type ="_doc",shards = 6,replicas = 3)
public class DiscussPost {

    // id组件
    @Id
    private int id;

    @Field(type = FieldType.Integer)
    private int userId;


    // 最复杂的就是title和content,analyzer是存储的时候解析器,searchAnalyzer搜索时候的解析器
    // 比如要存入的title是互联网校招,存入后需要给这句话建立索引,其实就是把这句话提炼出关键词,用关键词关联这句话,将来通过关键词去搜就可以通过关联搜到这句话
    // 那么存的时候就应该将这句话拆分为更多的词条,这样的话能够增加这句话搜索的范围,所以在保存的时候应该是把这句话尽可能拆分为多个词条与之匹配,增加搜索范围
    // 那么此时,就应该用一个分词词典非常大的一个分词器来尽可能多的匹配这句话的关键词,中文插件有一个ik_max_word刚好是分词最多的分词器
    // 打个比方能把这个互联网校招拆分为很多词,互联,互联网,联网,网校,校招
    // 将来我要搜的时候,那么互联网校招就没有必要拆分出那么多词了,搜索的时候我肯定只想知道和互联网有关的内容和校招有关的内容,都匹配是最好,搜索的时候不用分词分的那么细了
    // 而是应该能猜出我意思的去拆分,细粒度粗一点,会洞察意图,尽可能少的但是满足需要的这样的词汇
    @Field(type=FieldType.Text,analyzer = "ik_max_word",searchAnalyzer = "ik_smart")
    private String title;
    @Field(type=FieldType.Text,analyzer = "ik_max_word",searchAnalyzer = "ik_smart")
    private String content;

    @Field(type = FieldType.Integer)
    private int type;
    @Field(type = FieldType.Integer)
    private int status;
    @Field(type = FieldType.Date)
    private Date createTime;
    @Field(type = FieldType.Integer)
    private int commentCount;
    @Field(type = FieldType.Double)
    private double score;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "DiscussPost{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", createTime=" + createTime +
                ", commentCount=" + commentCount +
                ", score=" + score +
                '}';
    }

}
