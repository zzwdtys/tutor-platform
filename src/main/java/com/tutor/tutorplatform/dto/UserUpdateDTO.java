package com.tutor.tutorplatform.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private String nickname;
    private String phone;
    private String email;
    private String realName;
    private String school;
    private String grade;
    private String teachExp;
    private Integer role;         // 允许修改角色
    private String password;      // 可选，不为空时更新密码
}