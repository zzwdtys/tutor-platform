package com.tutor.tutorplatform.controller;

import com.tutor.tutorplatform.common.Result;
import com.tutor.tutorplatform.dto.SendMessageDTO;
import com.tutor.tutorplatform.entity.Conversation;
import com.tutor.tutorplatform.entity.Message;
import com.tutor.tutorplatform.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/message")
public class MessageController extends BaseController {

    @Autowired
    private MessageService messageService;

    @PostMapping("/send")
    public Result<Message> sendMessage(@RequestBody SendMessageDTO dto,
                                        HttpServletRequest request) {
        Long senderId = getUserIdFromRequest(request);
        Message msg = messageService.sendMessage(senderId, dto.getReceiverId(), dto.getContent());
        return Result.success(msg);
    }

    @GetMapping("/conversations")
    public Result<List<Conversation>> getConversations(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        return Result.success(messageService.getConversations(userId));
    }

    @GetMapping("/detail/{conversationId}")
    public Result<List<Message>> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        return Result.success(messageService.getMessages(conversationId, userId, page, pageSize));
    }

    @PostMapping("/read/{conversationId}")
    public Result<String> markAsRead(@PathVariable Long conversationId,
                                      HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        messageService.markAsRead(conversationId, userId);
        return Result.success("ok");
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Object>> getUnreadCount(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        Long count = messageService.getUnreadCount(userId);
        return Result.success(Map.of("count", count));
    }
}
