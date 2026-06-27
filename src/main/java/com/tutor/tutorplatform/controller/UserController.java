package com.tutor.tutorplatform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.tutor.tutorplatform.common.Result;
import com.tutor.tutorplatform.dto.*;
import com.tutor.tutorplatform.entity.*;
import com.tutor.tutorplatform.service.UserService;
import com.tutor.tutorplatform.service.WxLoginService;
import com.tutor.tutorplatform.service.AppointmentService;
import com.tutor.tutorplatform.service.ReviewService;
import com.tutor.tutorplatform.service.ResumeService;
import com.tutor.tutorplatform.service.DemandService;
import com.tutor.tutorplatform.utils.JwtUtil;
import com.tutor.tutorplatform.utils.PasswordUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController extends BaseController {

    @Autowired
    private WxLoginService wxLoginService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private DemandService demandService;

    @Value("${server.base-url}")
    private String baseUrl;

    // ==================== 微信登录（模拟/真实） ====================
    @PostMapping("/wxlogin")
    public Result<Map<String, Object>> wxLogin(@RequestParam String code) {
        String openid = wxLoginService.getSessionInfo(code).getOpenid();

        User user = userService.getByOpenid(openid);
        Map<String, Object> data = new HashMap<>();
        if (user != null) {
            // 老用户：直接生成 token 登录
            String token = jwtUtil.generateToken(user.getId(), user.getRole());
            data.put("token", token);
            data.put("userId", user.getId());
            data.put("role", user.getRole());
            data.put("needComplete", !user.getNickname().equals("微信用户") && user.getPhone() != null);
        } else {
            // 新用户：自动注册并生成 token
            User newUser = new User();
            newUser.setOpenid(openid);
            newUser.setRole(0);
            newUser.setNickname("微信用户");
            newUser.setStatus(1);
            userService.save(newUser);
            String token = jwtUtil.generateToken(newUser.getId(), newUser.getRole());
            data.put("token", token);
            data.put("userId", newUser.getId());
            data.put("role", newUser.getRole());
            data.put("needComplete", true);
        }
        return Result.success(data);
    }

    // ==================== 微信一键登录（带用户信息） ====================
    @PostMapping("/wxlogin2")
    public Result<Map<String, Object>> wxLoginWithProfile(@RequestParam String code,
                                                          @RequestParam(required = false) String nickname,
                                                          @RequestParam(required = false) String avatar) {
        String openid = wxLoginService.getSessionInfo(code).getOpenid();

        Map<String, Object> data = new HashMap<>();
        // 查询未删除的用户
        User user = userService.getByOpenid(openid);
        if (user != null) {
            String token = jwtUtil.generateToken(user.getId(), user.getRole());
            data.put("token", token);
            data.put("userId", user.getId());
            data.put("role", user.getRole());
            data.put("needComplete", false);
            return Result.success(data);
        }

        // 如果被逻辑删除过，尝试恢复
        User deleted = userService.getByOpenidIncludeDeleted(openid);
        if (deleted != null) {
            deleted.setIsDeleted(0);
            if (StringUtils.isNotBlank(nickname)) deleted.setNickname(nickname);
            if (StringUtils.isNotBlank(avatar)) deleted.setAvatar(avatar);
            userService.updateById(deleted);
            String token = jwtUtil.generateToken(deleted.getId(), deleted.getRole());
            data.put("token", token);
            data.put("userId", deleted.getId());
            data.put("role", deleted.getRole());
            data.put("needComplete", false);
            return Result.success(data);
        }

        // 真正的新用户：使用前端提供的 nickname/avatar 创建账号（默认学员 role=0）
        User newUser = new User();
        newUser.setOpenid(openid);
        newUser.setRole(0);
        newUser.setNickname(StringUtils.isBlank(nickname) ? "微信用户" : nickname);
        newUser.setAvatar(StringUtils.isBlank(avatar) ? null : avatar);
        newUser.setStatus(1);
        userService.save(newUser);
        String token = jwtUtil.generateToken(newUser.getId(), newUser.getRole());
        data.put("token", token);
        data.put("userId", newUser.getId());
        data.put("role", newUser.getRole());
        data.put("needComplete", true);
        return Result.success(data);
    }

    // ==================== 获取当前用户信息 ====================
    @GetMapping("/info")
    public Result<User> getUserInfo(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(401, "未登录或用户不存在");
        }
        // 头像处理：如果有自定义头像且非完整URL，拼接baseUrl
        // 默认头像路径（含 "default"）视为无头像，由前端根据角色显示本地默认头像
        if (StringUtils.isNotBlank(user.getAvatar()) && !user.getAvatar().contains("default")) {
            if (!user.getAvatar().startsWith("http") && !user.getAvatar().startsWith("data:")) {
                user.setAvatar(baseUrl + user.getAvatar());
            }
        } else {
            user.setAvatar(null);
        }
        user.setOpenid(null);
        return Result.success(user);
    }

    // ==================== 密码登录 ====================
    @PostMapping("/login")
    public Result<Map<String, Object>> loginByPassword(@RequestBody LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            return Result.error("用户名或密码不能为空");
        }
        User user = userService.getByUsername(username);
        if (user == null || !PasswordUtil.matches(password, user.getPassword())) {
            return Result.error("用户名或密码错误");
        }
        if (user.getStatus() != 1) {
            return Result.error("账户已被禁用");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("role", user.getRole());
        return Result.success(data);
    }

    // ==================== 手动注册 ====================
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody RegisterDTO registerDTO) {
        if (userService.getByUsername(registerDTO.getUsername()) != null) {
            return Result.error("用户名已存在");
        }
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(PasswordUtil.encode(registerDTO.getPassword()));
        user.setRole(registerDTO.getRole());
        user.setEmail(registerDTO.getEmail());
        user.setNickname(registerDTO.getNickname());
        // 不设置默认头像，由前端根据角色显示本地默认头像
        user.setStatus(1);
        userService.save(user);
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("role", user.getRole());
        return Result.success(data);
    }

    // ==================== 完善用户信息（微信登录新用户） ====================
    @PostMapping("/complete")
    public Result<Map<String, Object>> completeUserInfo(@RequestBody UserCompleteDTO dto, @RequestHeader("Authorization") String auth) {
        String token = auth.substring(7);
        Claims claims = jwtUtil.getClaimsFromToken(token);
        String openid = (String) claims.get("openid");
        if (openid == null) throw new RuntimeException("无效的临时凭证");
        if (userService.getByOpenid(openid) != null) {
            return Result.error("用户已存在");
        }
        User user = new User();
        user.setOpenid(openid);
        user.setRole(dto.getRole());
        user.setNickname(dto.getNickname());
        user.setPhone(dto.getPhone());
        user.setAvatar(dto.getAvatar());
        user.setEmail(dto.getEmail());
        user.setRealName(dto.getRealName());
        user.setSchool(dto.getSchool());
        user.setGrade(dto.getGrade());
        user.setTeachExp(dto.getTeachExp());
        user.setStatus(1);
        userService.save(user);
        String finalToken = jwtUtil.generateToken(user.getId(), user.getRole());
        Map<String, Object> data = new HashMap<>();
        data.put("token", finalToken);
        data.put("userId", user.getId());
        data.put("role", user.getRole());
        return Result.success(data);
    }

    // ==================== 修改个人资料 ====================
    @PutMapping("/profile")
    public Result<User> updateProfile(@RequestBody UserProfileDTO dto, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        User user = userService.getById(userId);
        if (user == null) return Result.error("用户不存在");

        if (StringUtils.isNotBlank(dto.getNickname())) user.setNickname(dto.getNickname());
        if (StringUtils.isNotBlank(dto.getPhone())) user.setPhone(dto.getPhone());
        if (StringUtils.isNotBlank(dto.getEmail())) user.setEmail(dto.getEmail());
        if (StringUtils.isNotBlank(dto.getRealName())) user.setRealName(dto.getRealName());
        if (StringUtils.isNotBlank(dto.getSchool())) user.setSchool(dto.getSchool());
        if (StringUtils.isNotBlank(dto.getGrade())) user.setGrade(dto.getGrade());
        if (StringUtils.isNotBlank(dto.getTeachExp())) user.setTeachExp(dto.getTeachExp());

        // 头像处理：如果是完整 URL，提取相对路径保存
        if (StringUtils.isNotBlank(dto.getAvatar())) {
            String avatar = dto.getAvatar();
            if (avatar.startsWith("http")) {
                int idx = avatar.indexOf("/uploads/avatars/");
                if (idx != -1) {
                    avatar = avatar.substring(idx);
                }
            }
            user.setAvatar(avatar);
        }
        userService.updateById(user);
        return Result.success(user);
    }

    // ==================== 注销账号（物理删除） ====================
    @DeleteMapping("/delete")
    @Transactional
    public Result<String> deleteOwnAccount(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(401, "未登录或用户不存在");
        }
        if (user.getRole() == 2) {
            return Result.error("管理员账号不能注销");
        }

        // 1. 删除用户的所有需求及关联的预约和评价
        List<Demand> demands = demandService.lambdaQuery().eq(Demand::getUserId, userId).list();
        for (Demand demand : demands) {
            List<Appointment> appointments = appointmentService.lambdaQuery().eq(Appointment::getDemandId, demand.getId()).list();
            for (Appointment apt : appointments) {
                reviewService.lambdaUpdate().eq(Review::getAppointmentId, apt.getId()).remove();
            }
            appointmentService.lambdaUpdate().eq(Appointment::getDemandId, demand.getId()).remove();
        }
        demandService.lambdaUpdate().eq(Demand::getUserId, userId).remove();

        // 2. 删除用户的简历（教员）及关联的预约和评价
        Resume resume = resumeService.lambdaQuery().eq(Resume::getUserId, userId).one();
        if (resume != null) {
            List<Appointment> appointments = appointmentService.lambdaQuery().eq(Appointment::getResumeId, resume.getId()).list();
            for (Appointment apt : appointments) {
                reviewService.lambdaUpdate().eq(Review::getAppointmentId, apt.getId()).remove();
            }
            appointmentService.lambdaUpdate().eq(Appointment::getResumeId, resume.getId()).remove();
            resumeService.removeById(resume.getId());
        }

        // 3. 删除用户作为学员或教员参与的预约
        List<Appointment> directApps = appointmentService.lambdaQuery()
                .eq(Appointment::getStudentId, userId)
                .or().eq(Appointment::getTeacherId, userId)
                .list();
        for (Appointment apt : directApps) {
            reviewService.lambdaUpdate().eq(Review::getAppointmentId, apt.getId()).remove();
        }
        appointmentService.lambdaUpdate()
                .eq(Appointment::getStudentId, userId)
                .or().eq(Appointment::getTeacherId, userId)
                .remove();

        // 4. 物理删除用户
        userService.physicalDeleteById(userId);

        return Result.success("账号已注销");
    }

    // ==================== 获取公开用户信息（用于展示） ====================
    @GetMapping("/public/{id}")
    public Result<User> getPublicUserInfo(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) return Result.error("用户不存在");
        User publicInfo = new User();
        publicInfo.setId(user.getId());
        publicInfo.setNickname(user.getNickname());
        publicInfo.setAvatar(user.getAvatar());
        publicInfo.setSchool(user.getSchool());
        publicInfo.setRole(user.getRole());
        if (StringUtils.isNotBlank(publicInfo.getAvatar())
                && !publicInfo.getAvatar().startsWith("http")
                && !publicInfo.getAvatar().startsWith("data:")
                && !publicInfo.getAvatar().contains("default")) {
            publicInfo.setAvatar(baseUrl + publicInfo.getAvatar());
        } else if (StringUtils.isNotBlank(publicInfo.getAvatar()) && publicInfo.getAvatar().contains("default")) {
            publicInfo.setAvatar(null);
        }
        return Result.success(publicInfo);
    }

    // ==================== 忘记密码 - 验证手机号 ====================
    @PostMapping("/reset-pwd/verify")
    public Result<Map<String, Object>> verifyPhoneForReset(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        if (StringUtils.isBlank(phone)) {
            return Result.error("请输入手机号");
        }
        User user = userService.lambdaQuery().eq(User::getPhone, phone).one();
        if (user == null) {
            return Result.error("该手机号未注册");
        }
        return Result.success(Map.of("userId", user.getId()));
    }

    // ==================== 忘记密码 - 重置密码 ====================
    @PostMapping("/reset-pwd")
    public Result<String> resetPassword(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        String newPassword = (String) body.get("newPassword");
        if (StringUtils.isBlank(newPassword) || newPassword.length() < 6) {
            return Result.error("密码至少6位");
        }
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(PasswordUtil.encode(newPassword));
        userService.updateById(user);
        return Result.success("密码重置成功");
    }
}