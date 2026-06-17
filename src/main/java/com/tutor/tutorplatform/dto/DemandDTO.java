package com.tutor.tutorplatform.dto;

import lombok.Data;

@Data
public class DemandDTO {
    private String subject;        // 科目
    private String grade;          // 年级
    private String location;       // 地点
    private Integer budgetMin;     // 预算下限
    private Integer budgetMax;     // 预算上限
    private Integer teacherGender; // 期望教员性别 0不限 1男 2女
    private String description;    // 需求描述（自然语言）
}