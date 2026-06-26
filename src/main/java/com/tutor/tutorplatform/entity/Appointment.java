package com.tutor.tutorplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("appointment")
public class Appointment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long demandId;
    private Long resumeId;
    private Long studentId;
    private Long teacherId;
    private Integer status;
    private Integer initiator;  // 0=学员发起 1=教员发起
    private Date appointmentTime;
    private Date actualTime;
    @TableField(exist = false)
    private String rejectReason;   // 教员拒绝理由
    @TableField(exist = false)
    private String cancelReason;   // 学员取消理由
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
    public static final int STATUS_PENDING = 0;   // 待接单
    public static final int STATUS_ACCEPTED = 1;  // 已接单
    public static final int STATUS_REJECTED = 2;  // 已拒绝
    public static final int STATUS_TEACHED = 3;   // 已授课
    public static final int STATUS_COMPLETED = 4; // 已完成
    public static final int STATUS_CANCELLED = 5; // 已取消
    @TableField(exist = false)
    private Boolean reviewed;
    @TableField(exist = false)
    private Review review;
    // 对方信息（用于列表展示）
    @TableField(exist = false)
    private Long otherPartyId;
    @TableField(exist = false)
    private String otherPartyNickname;
    @TableField(exist = false)
    private String otherPartyAvatar;
}