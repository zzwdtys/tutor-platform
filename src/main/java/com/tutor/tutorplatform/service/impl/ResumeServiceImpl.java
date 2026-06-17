package com.tutor.tutorplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tutor.tutorplatform.entity.Resume;
import com.tutor.tutorplatform.mapper.ResumeMapper;
import com.tutor.tutorplatform.service.ResumeService;
import com.tutor.tutorplatform.dto.ResumeDTO;
import org.springframework.stereotype.Service;

@Service
public class ResumeServiceImpl extends ServiceImpl<ResumeMapper, Resume> implements ResumeService {

    @Override
    public Resume createResume(Long userId, ResumeDTO dto) {
        // 查询该用户是否已有简历
        Resume existing = this.lambdaQuery().eq(Resume::getUserId, userId).one();
        if (existing != null) {
            // 更新现有简历
            existing.setSubjects(dto.getSubjects());
            existing.setGrades(dto.getGrades());
            existing.setPrice(dto.getPrice());
            existing.setLocation(dto.getLocation());
            existing.setTeachingYears(dto.getTeachingYears());
            existing.setCertificate(dto.getCertificate());
            existing.setSelfIntro(dto.getSelfIntro());
            // 如果简历原本是下架状态，更新后自动设为已发布（可根据需求调整）
            existing.setStatus(1);
            this.updateById(existing);
            return existing;
        } else {
            // 创建新简历
            Resume resume = new Resume();
            resume.setUserId(userId);
            resume.setSubjects(dto.getSubjects());
            resume.setGrades(dto.getGrades());
            resume.setPrice(dto.getPrice());
            resume.setLocation(dto.getLocation());
            resume.setTeachingYears(dto.getTeachingYears());
            resume.setCertificate(dto.getCertificate());
            resume.setSelfIntro(dto.getSelfIntro());
            resume.setStatus(1); // 已发布
            this.save(resume);
            return resume;
        }
    }

    @Override
    public Resume getByUserId(Long userId) {
        return this.getOne(new LambdaQueryWrapper<Resume>().eq(Resume::getUserId, userId));
    }

    @Override
    public Resume updateResume(Long userId, ResumeDTO dto) {
        Resume existing = lambdaQuery().eq(Resume::getUserId, userId).one();
        if (existing == null) {
            throw new RuntimeException("简历不存在");
        }
        existing.setSubjects(dto.getSubjects());
        existing.setGrades(dto.getGrades());
        existing.setPrice(dto.getPrice());
        existing.setLocation(dto.getLocation());
        existing.setTeachingYears(dto.getTeachingYears());
        existing.setCertificate(dto.getCertificate());
        existing.setSelfIntro(dto.getSelfIntro());
        updateById(existing);
        return existing;
    }
}