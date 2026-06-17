package com.tutor.tutorplatform.dto;

import lombok.Data;

@Data
public class SearchResumeDTO {
    private String subject;      // 科目
    private String grade;        // 年级
    private String location;     // 地区（模糊匹配）
    private Integer maxPrice;    // 最高价格
    private Integer minPrice;    // 最低价格
    private Integer sortType;    // 0-默认排序（匹配分） 1-价格升序 2-价格降序 3-评分降序
}