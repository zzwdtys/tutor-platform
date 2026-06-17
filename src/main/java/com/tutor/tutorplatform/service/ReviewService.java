package com.tutor.tutorplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tutor.tutorplatform.entity.Review;
import com.tutor.tutorplatform.dto.CreateReviewDTO;
import java.util.List;

public interface ReviewService extends IService<Review> {
    Review createReview(Long studentId, CreateReviewDTO dto);
    List<Review> getReviewsByTeacherId(Long teacherId);
}