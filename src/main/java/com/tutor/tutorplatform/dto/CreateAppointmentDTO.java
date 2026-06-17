package com.tutor.tutorplatform.dto;

import lombok.Data;

@Data
public class CreateAppointmentDTO {
    private Long demandId;    // 需求ID
    private Long resumeId;    // 简历ID（教员）
    private String appointmentTime;  // 期望上课时间，格式 "yyyy-MM-dd HH:mm:ss"
}