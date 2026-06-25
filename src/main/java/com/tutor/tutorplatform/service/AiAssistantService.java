package com.tutor.tutorplatform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutor.tutorplatform.entity.*;
import com.tutor.tutorplatform.mapper.AiConversationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class AiAssistantService {

    @Value("${ai.deepseek.api-key:NOT_SET}")
    private String apiKey;

    @Value("${ai.deepseek.base-url:https://api.deepseek.com/v1}")
    private String baseUrl;

    @Autowired private AiConversationMapper aiConversationMapper;
    @Autowired private DemandService demandService;
    @Autowired private ResumeService resumeService;
    @Autowired private SearchService searchService;
    @Autowired private UserService userService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String chat(Long userId, String message) {
        String sysPrompt = "你是在线家教服务平台的AI助手。请用中文回复。";
        try { User u = userService.getById(userId);
              if (u != null) sysPrompt += "当前用户：" + (u.getRole()==0?"学员":"教员"); }
        catch (Exception ignored) {}
        saveMsg(userId, "user", message);
        String reply = callDeepSeek(sysPrompt, message);
        saveMsg(userId, "assistant", reply);
        return reply;
    }

    public String recommendTeachers(Long userId, Long demandId) {
        Demand demand = demandId != null ? demandService.getById(demandId) : null;
        if (demand == null) {
            List<Demand> demands = demandService.lambdaQuery()
                .eq(Demand::getUserId, userId).orderByDesc(Demand::getCreateTime).list();
            if (!demands.isEmpty()) demand = demands.get(0);
        }
        List<Resume> matching = new ArrayList<>();
        if (demand != null) {
            matching = searchService.searchResumes(
                demand.getSubject(), demand.getGrade(), demand.getLocation(),
                demand.getBudgetMin(), demand.getBudgetMax(), 0);
        }
        StringBuilder p = new StringBuilder("请根据学员需求推荐教员：\n\n");
        if (demand != null) {
            p.append("科目：").append(demand.getSubject()).append("\n")
             .append("年级：").append(demand.getGrade()).append("\n")
             .append("地点：").append(demand.getLocation()).append("\n")
             .append("预算：").append(demand.getBudgetMin()).append("-")
             .append(demand.getBudgetMax()).append("元/h\n");
        }
        p.append("\n匹配教员：\n");
        for (int i = 0; i < Math.min(matching.size(), 5); i++) {
            Resume r = matching.get(i);
            p.append(i+1).append(". ").append(r.getSubjects())
             .append(" | ").append(r.getGrades())
             .append(" | ").append(r.getPrice()).append("元/h")
             .append(" | ").append(r.getLocation()).append("\n");
        }
        p.append("\n请推荐最合适的2-3位并说明理由。");
        String reply = callDeepSeek("你是家教平台AI助手，请用中文回复。", p.toString());
        saveMsg(userId, "user", "推荐教员");
        saveMsg(userId, "assistant", reply);
        return reply;
    }

    public String giveOptimizationAdvice(Long userId, String type) {
        StringBuilder p = new StringBuilder();
        if ("demand".equals(type)) {
            List<Demand> demands = demandService.lambdaQuery()
                .eq(Demand::getUserId, userId).orderByDesc(Demand::getCreateTime).list();
            if (demands.isEmpty()) {
                p.append("请建议如何写好一份家教需求。");
            } else {
                Demand d = demands.get(0);
                p.append("分析以下需求：\n科目：").append(d.getSubject())
                 .append("\n年级：").append(d.getGrade())
                 .append("\n地点：").append(d.getLocation())
                 .append("\n预算：").append(d.getBudgetMin()).append("-")
                 .append(d.getBudgetMax()).append("元/h\n");
                if (d.getDescription() != null) p.append("描述：").append(d.getDescription());
                p.append("\n请给出优化建议。");
            }
        } else {
            Resume r = resumeService.lambdaQuery().eq(Resume::getUserId, userId).one();
            if (r == null) {
                p.append("请建议如何写好一份教员简历。");
            } else {
                p.append("分析以下简历：\n科目：").append(r.getSubjects())
                 .append("\n年级：").append(r.getGrades())
                 .append("\n价格：").append(r.getPrice()).append("元/h")
                 .append("\n地点：").append(r.getLocation())
                 .append("\n教龄：").append(r.getTeachingYears()).append("年\n");
                if (r.getSelfIntro() != null) p.append("简介：").append(r.getSelfIntro());
                p.append("\n请给出优化建议。");
            }
        }
        String reply = callDeepSeek("你是家教平台AI助手，请给出具体的改进建议。", p.toString());
        saveMsg(userId, "user", "优化" + ("demand".equals(type) ? "需求" : "简历"));
        saveMsg(userId, "assistant", reply);
        return reply;
    }

    public List<AiConversation> getHistory(Long userId, int page, int pageSize) {
        return aiConversationMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiConversation>()
                .eq(AiConversation::getUserId, userId)
                .orderByDesc(AiConversation::getCreateTime)
                .last("LIMIT " + ((page-1)*pageSize) + ", " + pageSize));
    }

    private String callDeepSeek(String sys, String msg) {
        if ("NOT_SET".equals(apiKey) || apiKey == null || apiKey.isBlank()) {
            return "AI助手尚未配置API密钥，请在Railway环境变量中设置 AI_DEEPSEEK_API_KEY。";
        }
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", "deepseek-chat");
            List<Map<String, String>> msgs = new ArrayList<>();
            msgs.add(Map.of("role","system","content",sys));
            msgs.add(Map.of("role","user","content",msg));
            body.put("messages", msgs);
            body.put("max_tokens", 1024);
            body.put("temperature", 0.7);
            HttpHeaders h = new HttpHeaders();
            h.setContentType(MediaType.APPLICATION_JSON);
            h.set("Authorization", "Bearer " + apiKey);
            ResponseEntity<String> r = restTemplate.postForEntity(
                baseUrl + "/chat/completions", new HttpEntity<>(body, h), String.class);
            return objectMapper.readTree(r.getBody()).path("choices").get(0)
                .path("message").path("content").asText();
        } catch (Exception e) {
            return "AI助手暂时无法响应：" + e.getMessage();
        }
    }

    private void saveMsg(Long uid, String role, String content) {
        AiConversation m = new AiConversation();
        m.setUserId(uid); m.setRole(role); m.setContent(content);
        aiConversationMapper.insert(m);
    }
}
