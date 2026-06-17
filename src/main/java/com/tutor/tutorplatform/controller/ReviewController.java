package com.tutor.tutorplatform.controller;

import com.tutor.tutorplatform.common.Result;
import com.tutor.tutorplatform.dto.CreateReviewDTO;
import com.tutor.tutorplatform.entity.Review;
import com.tutor.tutorplatform.service.ReviewService;
import com.tutor.tutorplatform.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/review")
public class ReviewController extends BaseController {

    @Autowired
    private ReviewService reviewService;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/create")
    public Result<Review> createReview(@RequestBody CreateReviewDTO dto, HttpServletRequest request) {
        Long studentId = getUserIdFromRequest(request);
        Review review = reviewService.createReview(studentId, dto);
        return Result.success(review);
    }

    @GetMapping("/teacher/{teacherId}")
    public Result<List<Review>> getTeacherReviews(@PathVariable Long teacherId) {
        // 这里不强制登录，任何人都可以查看教员评价；也可加上权限校验
        return Result.success(reviewService.getReviewsByTeacherId(teacherId));
    }

    @GetMapping("/teacher/list")
    public Result<List<Review>> getTeacherReviews(HttpServletRequest request) {
        Long teacherId = getUserIdFromRequest(request);
        List<Review> list = reviewService.lambdaQuery()
                .eq(Review::getTeacherId, teacherId)
                .orderByDesc(Review::getCreateTime)
                .list();
        // 补充学员昵称（可选）
        return Result.success(list);
    }
}