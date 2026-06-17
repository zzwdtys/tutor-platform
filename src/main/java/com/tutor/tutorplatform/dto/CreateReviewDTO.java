package com.tutor.tutorplatform.dto;

import lombok.Data;

@Data
public class CreateReviewDTO {
    private Long appointmentId;   // 预约ID
    private Integer professionalism; // 专业水平分 1-5
    private Integer patience;        // 耐心分 1-5
    private Integer communication;   // 沟通能力分 1-5
    private String commentText;      // 文字评论
}