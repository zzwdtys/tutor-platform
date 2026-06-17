package com.tutor.tutorplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tutor.tutorplatform.entity.Appointment;
import com.tutor.tutorplatform.entity.Review;
import com.tutor.tutorplatform.mapper.ReviewMapper;
import com.tutor.tutorplatform.service.AppointmentService;
import com.tutor.tutorplatform.service.ReviewService;
import com.tutor.tutorplatform.dto.CreateReviewDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {

    @Autowired
    @Lazy
    private AppointmentService appointmentService;

    // 预定义积极词汇和标签提取关键词
    private static final Set<String> POSITIVE_WORDS = new HashSet<>(Arrays.asList(
            "好", "耐心", "专业", "负责", "清晰", "明白", "收获", "谢谢", "满意", "不错", "优秀", "厉害", "棒"
    ));
    private static final Set<String> NEGATIVE_WORDS = new HashSet<>(Arrays.asList(
            "差", "不好", "不耐心", "不专业", "敷衍", "听不懂", "浪费时间", "失望"
    ));
    private static final Map<String, String> TAG_KEYWORDS = new HashMap<>();
    static {
        TAG_KEYWORDS.put("耐心", "耐心");
        TAG_KEYWORDS.put("专业", "专业");
        TAG_KEYWORDS.put("负责", "负责");
        TAG_KEYWORDS.put("清晰", "讲解清晰");
        TAG_KEYWORDS.put("收获", "有收获");
        TAG_KEYWORDS.put("逻辑", "逻辑清晰");
    }

    @Override
    @Transactional
    public Review createReview(Long studentId, CreateReviewDTO dto) {
        // 1. 验证预约存在且属于该学员，且状态为已授课(3)或已完成(4)
        Appointment appointment = appointmentService.getById(dto.getAppointmentId());
        if (appointment == null || !appointment.getStudentId().equals(studentId)) {
            throw new RuntimeException("预约不存在或权限不足");
        }
        if (appointment.getStatus() != 3 && appointment.getStatus() != 4) {
            throw new RuntimeException("只有已授课的预约才能评价");
        }

        // 2. 防止重复评价（检查是否已有评价）
        long count = this.lambdaQuery().eq(Review::getAppointmentId, dto.getAppointmentId()).count();
        if (count > 0) {
            throw new RuntimeException("该预约已评价过");
        }

        // 3. 计算综合得分（平均分，保留一位小数）
        int total = dto.getProfessionalism() + dto.getPatience() + dto.getCommunication();
        int score = (int) Math.round(total / 3.0);

        // 4. 简单情感分析和标签提取
        String comment = dto.getCommentText() == null ? "" : dto.getCommentText();
        int sentiment = analyzeSentiment(comment);
        String tags = extractTags(comment);

        // 5. 创建评价记录
        Review review = new Review();
        review.setAppointmentId(dto.getAppointmentId());
        review.setStudentId(studentId);
        review.setTeacherId(appointment.getTeacherId());
        review.setScore(score);
        review.setProfessionalism(dto.getProfessionalism());
        review.setPatience(dto.getPatience());
        review.setCommunication(dto.getCommunication());
        review.setCommentText(comment);
        review.setSentiment(sentiment);
        review.setTags(tags);
        this.save(review);

        // 6. 更新预约状态为已完成(4)
        appointment.setStatus(4);
        appointmentService.updateById(appointment);

        return review;
    }

    @Override
    public List<Review> getReviewsByTeacherId(Long teacherId) {
        return this.lambdaQuery()
                .eq(Review::getTeacherId, teacherId)
                .orderByDesc(Review::getCreateTime)
                .list();
    }

    private int analyzeSentiment(String text) {
        if (text == null || text.isEmpty()) return 1; // 中性
        int positiveCount = 0;
        int negativeCount = 0;
        for (String word : POSITIVE_WORDS) {
            if (text.contains(word)) positiveCount++;
        }
        for (String word : NEGATIVE_WORDS) {
            if (text.contains(word)) negativeCount++;
        }
        if (positiveCount > negativeCount) return 2; // 积极
        if (negativeCount > positiveCount) return 0; // 消极
        return 1; // 中性
    }

    private String extractTags(String text) {
        if (text == null || text.isEmpty()) return "";
        List<String> foundTags = new ArrayList<>();
        for (Map.Entry<String, String> entry : TAG_KEYWORDS.entrySet()) {
            if (text.contains(entry.getKey())) {
                foundTags.add(entry.getValue());
            }
        }
        // 去重并限制最多3个标签
        foundTags = new ArrayList<>(new LinkedHashSet<>(foundTags));
        if (foundTags.size() > 3) foundTags = foundTags.subList(0, 3);
        return String.join(",", foundTags);
    }
}