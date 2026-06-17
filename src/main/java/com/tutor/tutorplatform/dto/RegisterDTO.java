package com.tutor.tutorplatform.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;
    private String password;
    private Integer role;   // 0学员 1教员
    private String email;
    private String nickname;
}