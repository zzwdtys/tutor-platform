package com.tutor.tutorplatform.dto;

import lombok.Data;

@Data
public class UserCreateDTO {
    private String username;      // 可选，用于密码登录
    private String password;      // 密码
    private Integer role;         // 0-学员 1-教员 2-管理员
    private String nickname;
    private String phone;
    private String email;
    private String realName;
    private String school;
    private String grade;
    private String teachExp;
    private Integer status;       // 可选，默认1
}