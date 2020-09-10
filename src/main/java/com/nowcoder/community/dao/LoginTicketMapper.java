package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
public interface LoginTicketMapper {

    // 大括号中可以写无数个""字符串,最后拼接在一起
    @Insert({
            "insert into login_ticket(user_id,ticket,status,expired) ","values(#{userId},#{ticket},#{status},#{expired})"
    })
    // 做声明,类似于user-mapper.xml中配置的insertUser方法,这是因为insert的时候,mysql底层会自动生成id,生成id后,mybatis会从mysql得到这个id,然后填入封装对象里,这个id是mybatis从数据库获取到自动回填的
    // useGeneratedKeys自动生成主键
    @Options(useGeneratedKeys = true ,keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    // 客户端用cookie存了这个数据,再次访问服务器时候,会把这个ticket给我,那么服务器就可以利用ticket查到整条数据,那就知道哪个用户在访问
    // ticket唯一标识,不可重复
    @Select({"select id,user_id,ticket,status,expired from login_ticket where ticket=#{ticket}"})
    LoginTicket selectByTicket(String ticket);

    @Update({"update login_ticket set status=#{status} where ticket=#{ticket}"})
    int updateStatus(String ticket, int status);

}
