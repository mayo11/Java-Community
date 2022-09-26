package com.mayo.community.dao;

import com.mayo.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.kafka.common.protocol.types.Field;

import java.util.List;

@Mapper
public interface MessageMapper {

    // 查询当前用户的会话列表，针对每一条会话只返回最新的一条私信
    List<Message> selectConversation(int userId, int offset, int limit);

    // 查询当前用户的会话数量
    int selectConversationCount(int userId);

    // 查询某个会话包含的私信列表
    List<Message> selectLetters(String conversationId, int offset, int limit);

    // 查询某个会话包含的私信数量
    int selectLetterCount(String conversationId);

    // 查询未读私信数量
    int selectLetterUnreadCount(int userId , String conversationId);

    // 查询某个主题所包含的通知列表
    List<Message> selectNotices(int userId, String topic, int offset, int limit);

    // 查询某个主题下最新的通知
    Message selectLatestNotice(int userId, String topic);

    // 查询某个主题所包含的通知数量
    int selectNoticeCount(int userId, String topic);

    // 查询某个主题未读通知数量
    int selectNoticeUnreadCount(int userId, String topic);

    // 新增私信
    int insertMessage(Message message);

    //修改消息状态
    int updateStatus(List<Integer> ids, int status);

}
