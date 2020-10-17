package com.nowcoder.community.actuator;

import com.nowcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;


// 编写完代码之后不用配置,直接由容器管理即可访问
// 不属于任何层次,通用的一个组件
@Component
// 通过endpoint注解配置,id配置是什么,之后访问就是通过/actuator/id访问
@Endpoint(id = "database")
public class DatabaseEndPoint {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseEndPoint.class);
    // 怎么看数据库当前连接是否正常呢?
    // 可以在调用端点的时候尝试去访问一下数据库,尝试去获取一个连接,能取到就OK,取不到就有问题
    // 尝试获取连接有多种方式,可以访问连接池,把DataSource注入进来,然后getConnection
    // 或者用更原始的方式,直接把连接参数注入进来,然后通过DriveManager来访问

    // 连接池是由Spring容易管理的,所以直接注入即可
    // 连接池的顶层接口就是DataSource
    @Autowired
    private DataSource dataSource;

    // 加一个注解@ReadOperation,表示这个方法是通过get请求来访问的,是一个get请求,如果是其他请求,要访问其他的operation,比如WriteOption
    @ReadOperation
    public String checkConnection(){
        try (Connection connection = dataSource.getConnection();
        ){
            return CommunityUtil.getJSONString(0,"获取连接成功!");
        } catch (SQLException throwables) {
            logger.error("获取连接失败:" + throwables.getMessage());
            return CommunityUtil.getJSONString(1,"获取连接失败!");
        }
    }
}
