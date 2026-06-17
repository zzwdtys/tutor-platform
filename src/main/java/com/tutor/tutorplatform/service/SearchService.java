package com.tutor.tutorplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.tutor.tutorplatform.entity.Resume;
import com.tutor.tutorplatform.entity.User;
import com.tutor.tutorplatform.mapper.ResumeMapper;
import com.tutor.tutorplatform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchService {
    @Autowired
    private ResumeMapper resumeMapper;
    @Autowired
    private UserService userService;
    @Value("${server.base-url}")
    private String baseUrl;

    public List<Resume> searchResumes(String subject, String grade, String location,
                                      Integer minPrice, Integer maxPrice, Integer sortType) {
        // 1. 原有查询逻辑（根据条件查询 resume）
        LambdaQueryWrapper<Resume> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Resume::getStatus, 1);  // 已发布
        if (StringUtils.isNotBlank(subject)) {
            wrapper.like(Resume::getSubjects, subject);
        }
        if (StringUtils.isNotBlank(grade)) {
            wrapper.like(Resume::getGrades, grade);
        }
        if (StringUtils.isNotBlank(location)) {
            wrapper.like(Resume::getLocation, location);
        }
        if (minPrice != null) {
            wrapper.ge(Resume::getPrice, minPrice);
        }
        if (maxPrice != null) {
            wrapper.le(Resume::getPrice, maxPrice);
        }
        List<Resume> list = resumeMapper.selectList(wrapper);
        if (list.isEmpty()) return list;

        // 2. 批量获取用户头像
        List<Long> userIds = list.stream().map(Resume::getUserId).collect(Collectors.toList());
        List<User> users = userService.listByIds(userIds);
        Map<Long, String> avatarMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u.getAvatar() != null ? u.getAvatar() : ""));

        // 3. 填充临时字段 avatar（并转换为完整 URL）
        list.forEach(resume -> {
            String avatar = avatarMap.get(resume.getUserId());
            if (StringUtils.isNotBlank(avatar) && !avatar.startsWith("http")) {
                avatar = baseUrl + avatar;
            }
            resume.setAvatar(avatar);
        });

        return list;
    }
}