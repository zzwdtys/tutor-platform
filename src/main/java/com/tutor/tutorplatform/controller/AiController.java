package com.tutor.tutorplatform.controller;

import com.tutor.tutorplatform.common.Result;
import com.tutor.tutorplatform.dto.AiChatDTO;
import com.tutor.tutorplatform.entity.AiConversation;
import com.tutor.tutorplatform.service.AiAssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController extends BaseController {

    @Autowired
    private AiAssistantService aiAssistantService;

    @PostMapping("/chat")
    public Result<Map<String, String>> chat(@RequestBody AiChatDTO dto,
                                             HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        String reply;
        if ("recommend_teacher".equals(dto.getActionType())) {
            reply = aiAssistantService.recommendTeachers(userId, dto.getDemandId());
        } else if ("optimize_demand".equals(dto.getActionType())) {
            reply = aiAssistantService.giveOptimizationAdvice(userId, "demand");
        } else if ("optimize_resume".equals(dto.getActionType())) {
            reply = aiAssistantService.giveOptimizationAdvice(userId, "resume");
        } else {
            reply = aiAssistantService.chat(userId, dto.getMessage());
        }
        return Result.success(Map.of("reply", reply));
    }

    @GetMapping("/history")
    public Result<List<AiConversation>> getHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        return Result.success(aiAssistantService.getHistory(userId, page, pageSize));
    }
}
