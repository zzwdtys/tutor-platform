package com.tutor.tutorplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tutor.tutorplatform.entity.Follow;
import com.tutor.tutorplatform.entity.Resume;
import com.tutor.tutorplatform.entity.User;
import com.tutor.tutorplatform.mapper.FollowMapper;
import com.tutor.tutorplatform.service.FollowService;
import com.tutor.tutorplatform.service.ResumeService;
import com.tutor.tutorplatform.service.ReviewService;
import com.tutor.tutorplatform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    @Value("${server.base-url}")
    private String baseUrl;

    @Autowired
    private ResumeService resumeService;
    @Autowired
    private UserService userService;
    @Autowired
    private ReviewService reviewService;

    private String fixAvatar(String avatar) {
        if (avatar == null || avatar.isEmpty()) return null;
        // 默认头像路径视为无头像，由前端显示本地默认头像
        if (avatar.contains("default")) return null;
        if (avatar.startsWith("http")) return avatar;
        return baseUrl + avatar;
    }

    @Override
    @Transactional
    public boolean toggleFollow(Long studentId, Long teacherId) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getStudentId, studentId).eq(Follow::getTeacherId, teacherId);
        Follow existing = this.getOne(wrapper);
        if (existing != null) {
            this.removeById(existing.getId());
            return false; // 已取消关注
        } else {
            Follow follow = new Follow();
            follow.setStudentId(studentId);
            follow.setTeacherId(teacherId);
            this.save(follow);
            return true; // 已关注
        }
    }

    @Override
    public boolean isFollowing(Long studentId, Long teacherId) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getStudentId, studentId).eq(Follow::getTeacherId, teacherId);
        return this.count(wrapper) > 0;
    }

    @Override
    public List<Follow> getFollowList(Long userId, Integer role) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        if (role != null && role == 1) {
            // 教员：查关注了哪些学员（by teacher_id）
            wrapper.eq(Follow::getTeacherId, userId);
        } else {
            // 学员：查关注了哪些教员（by student_id）
            wrapper.eq(Follow::getStudentId, userId);
        }
        wrapper.orderByDesc(Follow::getCreateTime);
        List<Follow> follows = this.list(wrapper);
        if (follows.isEmpty()) return follows;

        if (role != null && role == 1) {
            // 教员视角：填充学员信息
            List<Long> studentIds = follows.stream().map(Follow::getStudentId).collect(Collectors.toList());
            List<User> users = userService.listByIds(studentIds);
            Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
            for (Follow follow : follows) {
                User student = userMap.get(follow.getStudentId());
                if (student != null) {
                    follow.setTeacherNickname(student.getNickname());
                    follow.setTeacherAvatar(fixAvatar(student.getAvatar()));
                }
            }
        } else {
            // 学员视角：填充教员信息
            List<Long> teacherIds = follows.stream().map(Follow::getTeacherId).collect(Collectors.toList());
            List<User> users = userService.listByIds(teacherIds);
            Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
            List<Resume> resumes = resumeService.lambdaQuery().in(Resume::getUserId, teacherIds).list();
            Map<Long, Resume> resumeMap = resumes.stream().collect(Collectors.toMap(Resume::getUserId, r -> r));
            for (Follow follow : follows) {
                User teacher = userMap.get(follow.getTeacherId());
                if (teacher != null) {
                    follow.setTeacherNickname(teacher.getNickname());
                    follow.setTeacherAvatar(fixAvatar(teacher.getAvatar()));
                }
                Resume resume = resumeMap.get(follow.getTeacherId());
                if (resume != null) {
                    follow.setTeacherSubjects(resume.getSubjects());
                    follow.setTeacherPrice(resume.getPrice());
                }
            }
        }
        return follows;
    }

    @Override
    public Long getFollowCount(Long studentId) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getStudentId, studentId);
        return this.count(wrapper);
    }
}
