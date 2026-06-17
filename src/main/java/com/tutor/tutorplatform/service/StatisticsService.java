package com.tutor.tutorplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tutor.tutorplatform.dto.StatisticsDTO;
import com.tutor.tutorplatform.entity.*;
import com.tutor.tutorplatform.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DemandMapper demandMapper;
    @Autowired
    private ResumeMapper resumeMapper;
    @Autowired
    private AppointmentMapper appointmentMapper;

    public StatisticsDTO getStatistics() {
        StatisticsDTO dto = new StatisticsDTO();

        // 1. 总数统计
        dto.setTotalUsers(userMapper.selectCount(null));
        dto.setTotalDemands(demandMapper.selectCount(null));
        dto.setTotalResumes(resumeMapper.selectCount(null));
        Long totalAppointments = appointmentMapper.selectCount(null);
        dto.setTotalAppointments(totalAppointments);

        // 2. 成功率（已接单 status=1 的预约数 / 总预约数）
        Long acceptedCount = appointmentMapper.selectCount(
                new LambdaQueryWrapper<Appointment>().eq(Appointment::getStatus, 1));
        dto.setSuccessRate(totalAppointments == 0 ? 0 : acceptedCount * 100.0 / totalAppointments);

        // 3. 科目热度排行（从 demand 表中按 subject 分组统计）
        // 注意：这里需要执行一个分组查询，可以用 MyBatis-Plus 的 selectMaps
        // 简单起见，我们使用原生 SQL 或 Lambda 查询后内存分组（小数据量可行）
        List<Demand> demands = demandMapper.selectList(null);
        Map<String, Long> subjectCount = demands.stream()
                .collect(Collectors.groupingBy(Demand::getSubject, Collectors.counting()));
        List<Map<String, Object>> subjectHotRank = subjectCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("subject", entry.getKey());
                    map.put("count", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());
        dto.setSubjectHotRank(subjectHotRank);

        // 4. 近7天新增需求趋势
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> weeklyTrend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(formatter);
            long demandCount = demandMapper.selectCount(
                    new LambdaQueryWrapper<Demand>()
                            .apply("DATE(create_time) = {0}", date.toString()));
            long appointmentCount = appointmentMapper.selectCount(
                    new LambdaQueryWrapper<Appointment>()
                            .apply("DATE(create_time) = {0}", date.toString()));
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", dateStr);
            dayData.put("demands", demandCount);
            dayData.put("appointments", appointmentCount);
            weeklyTrend.add(dayData);
        }
        dto.setWeeklyTrend(weeklyTrend);

        // 5. 地域分布（按 location 前两个字简单分组）
        List<Demand> allDemands = demandMapper.selectList(null);
        Map<String, Long> locationCount = allDemands.stream()
                .map(d -> {
                    String loc = d.getLocation();
                    if (loc != null && loc.length() >= 2) return loc.substring(0, 2);
                    return "其他";
                })
                .collect(Collectors.groupingBy(loc -> loc, Collectors.counting()));
        List<Map<String, Object>> locationDist = locationCount.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", entry.getKey());
                    map.put("value", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());
        dto.setLocationDist(locationDist);

        return dto;
    }
}