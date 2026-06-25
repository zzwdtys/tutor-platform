package com.tutor.tutorplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("ai_conversation")
public class AiConversation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String role;    // "user" 或 "assistant"
    private String content;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}
