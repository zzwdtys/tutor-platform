package com.tutor.tutorplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tutor.tutorplatform.entity.Resume;
import com.tutor.tutorplatform.dto.ResumeDTO;

public interface ResumeService extends IService<Resume> {
    Resume createResume(Long userId, ResumeDTO resumeDTO);
    Resume getByUserId(Long userId);
    Resume updateResume(Long userId, ResumeDTO resumeDTO);
}