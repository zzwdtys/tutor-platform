package com.tutor.tutorplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tutor.tutorplatform.entity.Conversation;
import com.tutor.tutorplatform.entity.Message;
import com.tutor.tutorplatform.entity.User;
import com.tutor.tutorplatform.mapper.ConversationMapper;
import com.tutor.tutorplatform.mapper.MessageMapper;
import com.tutor.tutorplatform.service.MessageService;
import com.tutor.tutorplatform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Value("${server.base-url}")
    private String baseUrl;

    @Autowired
    private ConversationMapper conversationMapper;
    @Autowired
    private UserService userService;

    private String fixAvatar(String avatar) {
        if (avatar == null || avatar.isEmpty()) return null;
        // 默认头像路径视为无头像，由前端显示本地默认头像
        if (avatar.contains("default")) return null;
        if (avatar.startsWith("http")) return avatar;
        return baseUrl + avatar;
    }

    @Override
    @Transactional
    public Message sendMessage(Long senderId, Long receiverId, String content) {
        if (senderId.equals(receiverId)) {
            throw new RuntimeException("不能给自己发送消息");
        }
        // 获取或创建会话
        Conversation conv = getOrCreateConversation(senderId, receiverId);
        // 保存消息
        Message msg = new Message();
        msg.setConversationId(conv.getId());
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setContent(content);
        msg.setIsRead(0);
        this.save(msg);
        // 更新会话最后消息
        conv.setLastMessage(content.length() > 50 ? content.substring(0, 50) + "..." : content);
        conv.setLastTime(new Date());
        if (conv.getUser1Id().equals(receiverId)) {
            conv.setUnreadCountUser1(conv.getUnreadCountUser1() == null ? 1 : conv.getUnreadCountUser1() + 1);
        } else {
            conv.setUnreadCountUser2(conv.getUnreadCountUser2() == null ? 1 : conv.getUnreadCountUser2() + 1);
        }
        conversationMapper.updateById(conv);
        return msg;
    }

    @Override
    public List<Conversation> getConversations(Long userId) {
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getUser1Id, userId).or().eq(Conversation::getUser2Id, userId);
        wrapper.orderByDesc(Conversation::getLastTime);
        List<Conversation> list = conversationMapper.selectList(wrapper);
        if (list.isEmpty()) return list;

        // 填充对方用户信息
        List<Long> otherIds = list.stream().map(c -> {
            Long other = c.getUser1Id().equals(userId) ? c.getUser2Id() : c.getUser1Id();
            c.setOtherUserId(other);
            return other;
        }).collect(Collectors.toList());

        List<User> users = userService.listByIds(otherIds);
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));

        for (Conversation conv : list) {
            User other = userMap.get(conv.getOtherUserId());
            if (other != null) {
                conv.setOtherNickname(other.getNickname());
                conv.setOtherAvatar(fixAvatar(other.getAvatar()));
            }
        }
        return list;
    }

    @Override
    public List<Message> getMessages(Long conversationId, Long userId, int page, int pageSize) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getConversationId, conversationId)
               .orderByDesc(Message::getCreateTime);
        Page<Message> pageObj = new Page<>(page, pageSize);
        Page<Message> result = this.page(pageObj, wrapper);
        // 倒序排列（最新在下面）
        List<Message> list = result.getRecords();
        java.util.Collections.reverse(list);

        // 填充发送者昵称（用循环避免 Collectors.toMap 遇到 null 值抛 NPE）
        List<Long> senderIds = list.stream().map(Message::getSenderId).distinct().collect(Collectors.toList());
        if (!senderIds.isEmpty()) {
            List<User> senders = userService.listByIds(senderIds);
            Map<Long, String> nameMap = new java.util.HashMap<>();
            Map<Long, String> avatarMap = new java.util.HashMap<>();
            for (User u : senders) {
                nameMap.put(u.getId(), u.getNickname());
                avatarMap.put(u.getId(), fixAvatar(u.getAvatar()));
            }
            for (Message msg : list) {
                msg.setSenderNickname(nameMap.get(msg.getSenderId()));
                msg.setSenderAvatar(avatarMap.get(msg.getSenderId()));
            }
        }
        return list;
    }

    @Override
    @Transactional
    public void markAsRead(Long conversationId, Long userId) {
        Conversation conv = conversationMapper.selectById(conversationId);
        if (conv == null) return;
        if (conv.getUser1Id().equals(userId)) {
            conv.setUnreadCountUser1(0);
        } else {
            conv.setUnreadCountUser2(0);
        }
        conversationMapper.updateById(conv);
        // 标记消息为已读
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getConversationId, conversationId)
               .eq(Message::getReceiverId, userId)
               .eq(Message::getIsRead, 0);
        Message updateMsg = new Message();
        updateMsg.setIsRead(1);
        this.update(updateMsg, wrapper);
    }

    @Override
    public Long getUnreadCount(Long userId) {
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Conversation::getUser1Id, userId).gt(Conversation::getUnreadCountUser1, 0)
                  .or().eq(Conversation::getUser2Id, userId).gt(Conversation::getUnreadCountUser2, 0));
        List<Conversation> list = conversationMapper.selectList(wrapper);
        long total = 0;
        for (Conversation conv : list) {
            total += conv.getUser1Id().equals(userId) ?
                     (conv.getUnreadCountUser1() != null ? conv.getUnreadCountUser1() : 0) :
                     (conv.getUnreadCountUser2() != null ? conv.getUnreadCountUser2() : 0);
        }
        return total;
    }

    @Override
    @Transactional
    public Conversation getOrCreateConversation(Long user1Id, Long user2Id) {
        // 确保 user1Id < user2Id 以保持一致性
        Long smaller = Math.min(user1Id, user2Id);
        Long larger = Math.max(user1Id, user2Id);
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getUser1Id, smaller).eq(Conversation::getUser2Id, larger);
        Conversation conv = conversationMapper.selectOne(wrapper);
        if (conv == null) {
            conv = new Conversation();
            conv.setUser1Id(smaller);
            conv.setUser2Id(larger);
            conv.setUnreadCountUser1(0);
            conv.setUnreadCountUser2(0);
            conversationMapper.insert(conv);
        }
        return conv;
    }
}
