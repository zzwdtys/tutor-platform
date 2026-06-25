package com.tutor.tutorplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("follow")
public class Follow {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long teacherId;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    // 非数据库字段，用于前端展示
    @TableField(exist = false)
    private String teacherNickname;
    @TableField(exist = false)
    private String teacherAvatar;
    @TableField(exist = false)
    private String teacherSubjects;
    @TableField(exist = false)
    private Integer teacherPrice;
    @TableField(exist = false)
    private Double teacherRating;
}
