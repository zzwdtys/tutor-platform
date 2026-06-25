package com.tutor.tutorplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tutor.tutorplatform.entity.Conversation;
import com.tutor.tutorplatform.entity.Message;
import java.util.List;

public interface MessageService extends IService<Message> {
    /** 发送消息，返回消息对象 */
    Message sendMessage(Long senderId, Long receiverId, String content);
    /** 获取当前用户的会话列表 */
    List<Conversation> getConversations(Long userId);
    /** 获取会话的聊天记录 */
    List<Message> getMessages(Long conversationId, Long userId, int page, int pageSize);
    /** 标记会话已读 */
    void markAsRead(Long conversationId, Long userId);
    /** 获取未读消息总数 */
    Long getUnreadCount(Long userId);
    /** 获取或创建会话 */
    Conversation getOrCreateConversation(Long user1Id, Long user2Id);
}
