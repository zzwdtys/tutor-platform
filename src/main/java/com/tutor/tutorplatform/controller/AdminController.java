package com.tutor.tutorplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.tutor.tutorplatform.common.Result;
import com.tutor.tutorplatform.dto.UserCreateDTO;
import com.tutor.tutorplatform.dto.UserUpdateDTO;
import com.tutor.tutorplatform.entity.*;
import com.tutor.tutorplatform.service.AppointmentService;
import com.tutor.tutorplatform.service.DemandService;
import com.tutor.tutorplatform.service.ResumeService;
import com.tutor.tutorplatform.service.UserService;
import com.tutor.tutorplatform.service.ReviewService;
import com.tutor.tutorplatform.utils.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private DemandService demandService;

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private ReviewService reviewService;

    private void checkAdmin(HttpServletRequest request) {
        Integer role = getRoleFromRequest(request);
        if (role == null || role != 2) {
            throw new RuntimeException("权限不足，仅管理员可操作");
        }
    }

    // 获取用户列表（支持角色和关键词筛选）
    @GetMapping("/users")
    public Result<List<User>> getUsers(
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        checkAdmin(request);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (role != null) {
            wrapper.eq(User::getRole, role);
        }
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(User::getNickname, keyword).or().like(User::getPhone, keyword));
        }
        wrapper.orderByDesc(User::getCreateTime);
        List<User> users = userService.list(wrapper);
        // 隐藏敏感字段
        users.forEach(u -> u.setOpenid(null));
        return Result.success(users);
    }

    // 更新用户状态
    @PutMapping("/user/status/{id}")
    public Result<String> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        boolean success = userService.lambdaUpdate()
                .eq(User::getId, id)
                .set(User::getStatus, status)
                .update();
        return success ? Result.success("操作成功") : Result.error("操作失败");
    }

    // 新增用户（管理员可创建任意角色的用户）
    @PostMapping("/user/create")
    public Result<User> createUser(@RequestBody UserCreateDTO dto) {
        // 检查用户名是否已存在（如果有 username）
        if (StringUtils.isNotBlank(dto.getUsername()) && userService.getByUsername(dto.getUsername()) != null) {
            return Result.error("用户名已存在");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(PasswordUtil.encode(dto.getPassword()));
        user.setRole(dto.getRole());
        user.setNickname(dto.getNickname());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setRealName(dto.getRealName());
        user.setSchool(dto.getSchool());
        user.setGrade(dto.getGrade());
        user.setTeachExp(dto.getTeachExp());
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        userService.save(user);
        return Result.success(user);
    }

    // 获取用户详情
    @GetMapping("/user/{id}")
    public Result<User> getUserDetail(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) return Result.error("用户不存在");
        user.setOpenid(null); // 隐藏敏感信息
        return Result.success(user);
    }

    // 更新用户信息（管理员可修改任意字段）
    @PutMapping("/user/update/{id}")
    public Result<User> updateUser(@PathVariable Long id, @RequestBody UserUpdateDTO dto) {
        User user = userService.getById(id);
        if (user == null) return Result.error("用户不存在");
        // 更新允许修改的字段
        user.setNickname(dto.getNickname());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setRealName(dto.getRealName());
        user.setSchool(dto.getSchool());
        user.setGrade(dto.getGrade());
        user.setTeachExp(dto.getTeachExp());
        user.setRole(dto.getRole());   // 允许管理员修改角色
        if (StringUtils.isNotBlank(dto.getPassword())) {
            user.setPassword(PasswordUtil.encode(dto.getPassword()));
        }
        userService.updateById(user);
        return Result.success(user);
    }

    @GetMapping("/demands")
    public Result<List<Demand>> getAllDemands(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Demand> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Demand::getSubject, keyword).or().like(Demand::getDescription, keyword));
        }
        wrapper.orderByDesc(Demand::getCreateTime);
        List<Demand> demands = demandService.list(wrapper);
        return Result.success(demands);
    }

    @Transactional
    @DeleteMapping("/demand/{id}")
    public Result<String> deleteDemand(@PathVariable Long id) {
        // 1. 查询该需求下所有预约
        List<Appointment> appointments = appointmentService.lambdaQuery()
                .eq(Appointment::getDemandId, id)
                .list();
        // 2. 删除每个预约对应的评价（如果有）
        for (Appointment apt : appointments) {
            reviewService.lambdaUpdate().eq(Review::getAppointmentId, apt.getId()).remove();
        }
        // 3. 删除预约记录
        appointmentService.lambdaUpdate().eq(Appointment::getDemandId, id).remove();
        // 4. 删除需求本身
        boolean success = demandService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @PutMapping("/demand/status/{id}")
    public Result<String> updateDemandStatus(@PathVariable Long id, @RequestParam Integer status) {
        boolean success = demandService.lambdaUpdate()
                .eq(Demand::getId, id)
                .set(Demand::getStatus, status)
                .update();
        return success ? Result.success("操作成功") : Result.error("操作失败");
    }

    // 获取所有简历（支持关键词搜索）
    @GetMapping("/resumes")
    public Result<List<Resume>> getAllResumes(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Resume> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Resume::getSubjects, keyword).or().like(Resume::getSelfIntro, keyword));
        }
        wrapper.orderByDesc(Resume::getCreateTime);
        List<Resume> list = resumeService.list(wrapper);
        return Result.success(list);
    }

    // 更新简历状态
    @PutMapping("/resume/status/{id}")
    public Result<String> updateResumeStatus(@PathVariable Long id, @RequestParam Integer status) {
        boolean success = resumeService.lambdaUpdate()
                .eq(Resume::getId, id)
                .set(Resume::getStatus, status)
                .update();
        return success ? Result.success("操作成功") : Result.error("操作失败");
    }


    // 获取所有预约（支持状态筛选）
    @GetMapping("/appointments")
    public Result<List<Appointment>> getAllAppointments(@RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Appointment::getStatus, status);
        }




        wrapper.orderByDesc(Appointment::getCreateTime);
        List<Appointment> list = appointmentService.list(wrapper);
        // 可选：为每个预约补充学员和教员昵称（如需在前端显示，可额外查询）
        return Result.success(list);
    }

    // 管理员修改预约状态
    @PutMapping("/appointment/status/{id}")
    public Result<String> updateAppointmentStatus(@PathVariable Long id, @RequestParam Integer status) {
        boolean success = appointmentService.lambdaUpdate()
                .eq(Appointment::getId, id)
                .set(Appointment::getStatus, status)
                .update();
        return success ? Result.success("操作成功") : Result.error("操作失败");
    }
}