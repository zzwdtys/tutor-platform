package com.tutor.tutorplatform.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class StatisticsDTO {
    private Long totalUsers;          // 总用户数
    private Long totalDemands;        // 总需求数
    private Long totalResumes;        // 总简历数
    private Long totalAppointments;   // 总预约数
    private Double successRate;       // 匹配成功率（已接单/总预约）
    private List<Map<String, Object>> subjectHotRank;   // 科目热度排行
    private List<Map<String, Object>> weeklyTrend;      // 近7天新增需求/预约趋势
    private List<Map<String, Object>> locationDist;     // 地域分布（按城市）
}