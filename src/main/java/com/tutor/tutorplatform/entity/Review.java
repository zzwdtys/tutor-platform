package com.tutor.tutorplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("review")
public class Review {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long appointmentId;
    private Long studentId;
    private Long teacherId;
    private Integer score;
    private Integer professionalism;
    private Integer patience;
    private Integer communication;
    private String commentText;
    private Integer sentiment;
    private String tags;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}