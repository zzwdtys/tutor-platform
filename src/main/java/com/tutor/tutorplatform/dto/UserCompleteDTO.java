package com.tutor.tutorplatform.dto;
import lombok.Data;
@Data
public class UserCompleteDTO {
    private Integer role;      // 0学员 1教员
    private String nickname;
    private String phone;
    private String avatar;
    private String email;
    private String realName;
    private String school;
    private String grade;
    private String teachExp;
}