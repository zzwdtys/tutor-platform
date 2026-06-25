package com.tutor.tutorplatform.controller;

import com.tutor.tutorplatform.common.Result;
import com.tutor.tutorplatform.entity.Follow;
import com.tutor.tutorplatform.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/follow")
public class FollowController extends BaseController {

    @Autowired
    private FollowService followService;

    @PostMapping("/toggle/{teacherId}")
    public Result<Map<String, Object>> toggleFollow(
            @PathVariable Long teacherId, HttpServletRequest request) {
        Long studentId = getUserIdFromRequest(request);
        boolean following = followService.toggleFollow(studentId, teacherId);
        return Result.success(Map.of("following", following));
    }

    @GetMapping("/check/{teacherId}")
    public Result<Map<String, Object>> checkFollow(
            @PathVariable Long teacherId, HttpServletRequest request) {
        Long studentId = getUserIdFromRequest(request);
        boolean following = followService.isFollowing(studentId, teacherId);
        return Result.success(Map.of("following", following));
    }

    @GetMapping("/list")
    public Result<List<Follow>> getFollowList(HttpServletRequest request) {
        Long studentId = getUserIdFromRequest(request);
        return Result.success(followService.getFollowList(studentId));
    }
}
