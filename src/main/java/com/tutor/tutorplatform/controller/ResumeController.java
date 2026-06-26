package com.tutor.tutorplatform.controller;

import com.tutor.tutorplatform.common.Result;
import com.tutor.tutorplatform.dto.ResumeDTO;
import com.tutor.tutorplatform.entity.Resume;
import com.tutor.tutorplatform.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/resume")
public class ResumeController extends BaseController {

    @Autowired
    private ResumeService resumeService;

    @PostMapping("/create")
    public Result<Resume> createResume(@RequestBody ResumeDTO resumeDTO, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        // 检查用户角色是否为教员（role=1）
        Integer role = getRoleFromRequest(request);
        if (role != 1) {
            return Result.error("仅教员可发布简历");
        }
        Resume resume = resumeService.createResume(userId, resumeDTO);
        return Result.success(resume);
    }

    @GetMapping("/my")
    public Result<Resume> getMyResume(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        Resume resume = resumeService.getByUserId(userId);
        return Result.success(resume);
    }

    @PutMapping("/update")
    public Result<Resume> updateResume(@RequestBody ResumeDTO resumeDTO, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        Resume resume = resumeService.updateResume(userId, resumeDTO);
        return Result.success(resume);
    }

    @GetMapping("/{id}")
    public Result<Resume> getResumeById(@PathVariable Long id) {
        Resume resume = resumeService.getById(id);
        if (resume == null) {
            return Result.error("简历不存在");
        }
        return Result.success(resume);
    }

    @GetMapping("/user/{userId}")
    public Result<Resume> getResumeByUserId(@PathVariable Long userId) {
        Resume resume = resumeService.getByUserId(userId);
        if (resume == null) {
            return Result.error("该用户暂无简历");
        }
        return Result.success(resume);
    }
}