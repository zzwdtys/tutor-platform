package com.tutor.tutorplatform.dto;

import lombok.Data;

@Data
public class AiChatDTO {
    private String message;      // 用户消息
    private String actionType;   // "chat" / "recommend_teacher" / "optimize_demand" / "optimize_resume"
    private Long demandId;       // 关联需求ID（推荐教员时用）
    private Long resumeId;       // 关联简历ID（推荐需求时用）
}
