package com.tutor.tutorplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("message")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long conversationId;
    private Long senderId;
    private Long receiverId;
    private String content;
    private Integer isRead;  // 0-未读 1-已读
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    // 非数据库字段
    @TableField(exist = false)
    private String senderNickname;
    @TableField(exist = false)
    private String senderAvatar;
}
