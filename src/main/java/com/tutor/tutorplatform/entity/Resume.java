package com.tutor.tutorplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("resume")
public class Resume {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String subjects;
    private String grades;
    private Integer price;
    private String location;
    private Integer teachingYears;
    private String certificate;
    private String selfIntro;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(exist = false)
    private String nickname;  // 教员昵称
    @TableField(exist = false)
    private String avatar;    // 教员头像
}