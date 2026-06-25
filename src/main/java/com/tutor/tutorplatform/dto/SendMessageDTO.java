package com.tutor.tutorplatform.dto;

import lombok.Data;

@Data
public class SendMessageDTO {
    private Long receiverId;
    private String content;
}
