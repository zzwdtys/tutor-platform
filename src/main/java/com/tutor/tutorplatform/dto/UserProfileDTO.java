package com.tutor.tutorplatform.dto;

import lombok.Data;

@Data
public class UserProfileDTO {
    private String nickname;
    private String phone;
    private String email;
    private String avatar;
    private String realName;
    private String school;
    private String grade;
    private String teachExp;
}