package com.tutor.tutorplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String openid;
    private Integer role;
    private String nickname;
    private String avatar;
    private String phone;
    private String realName;
    private String idCard;
    private String school;
    private String grade;
    private String teachExp;
    private Integer status;
    private String username;
    private String password;
    private String email;
    @TableLogic
    private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}