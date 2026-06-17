package com.tutor.tutorplatform.dto;

import lombok.Data;

@Data
public class ResumeDTO {
    private String subjects;       // 可授科目，逗号分隔
    private String grades;         // 可教年级
    private Integer price;         // 课时费
    private String location;       // 授课区域
    private Integer teachingYears; // 教龄
    private String certificate;    // 证书图片URL
    private String selfIntro;      // 自我介绍
}