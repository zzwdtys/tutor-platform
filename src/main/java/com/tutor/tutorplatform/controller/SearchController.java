package com.tutor.tutorplatform.controller;

import com.tutor.tutorplatform.common.Result;
import com.tutor.tutorplatform.dto.SearchResumeDTO;
import com.tutor.tutorplatform.entity.Resume;
import com.tutor.tutorplatform.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @PostMapping("/resumes")
    public Result<List<Resume>> searchResumes(@RequestBody SearchResumeDTO dto) {
        List<Resume> list = searchService.searchResumes(
                dto.getSubject(),
                dto.getGrade(),
                dto.getLocation(),
                dto.getMinPrice(),
                dto.getMaxPrice(),
                dto.getSortType()
        );
        return Result.success(list);
    }
}