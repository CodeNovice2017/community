package com.nowcoder.community.dao;


import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    // 查询当前用户会话列表,针对每个会话只返回一条最新的私信
    List<Message> selectConversations(int userId,int offset,int limit);

    // 查询当前用户的会话数量
    int selectConversationsCount(int userId);

    // 查询某个会话所包含的私信列表,详情页面时用到
    // 根据和某一个人的会话来查
    List<Message> selectLetters(String conversationId,int offset,int limit);

    // 查询某个会话所包含的私信数量
    int selectLettersCount(String conversationId);

    // 查询未读的私信消息数量
    // conversationId作为动态的条件拼,不是一定会有,这样这个方法就可以实现两种业务
    // 查询用户所有的未读消息数量
    // 查询用户的某个会话的私信未读数量
    int selectUnreadLettersCount(int userId,String conversationId);

    // 发送私信
    int insertLetter(Message message);

    // 更新多个未读消息的状态为已读,也可以设置删除
    int updateStatus(List<Integer> ids ,int status);

}
