package com.tutor.tutorplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("demand")
public class Demand {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String subject;
    private String grade;
    private String location;
    private Integer budgetMin;
    private Integer budgetMax;
    private Integer teacherGender;
    private String description;
    private Integer status;
    @TableField(exist = false)
    private Integer viewCount;     // 浏览量
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    // 非数据库字段
    @TableField(exist = false)
    private String userNickname;
    @TableField(exist = false)
    private String userAvatar;
}