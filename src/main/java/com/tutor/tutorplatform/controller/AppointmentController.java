package com.tutor.tutorplatform.controller;

import com.tutor.tutorplatform.common.Result;
import com.tutor.tutorplatform.dto.CreateAppointmentDTO;
import com.tutor.tutorplatform.entity.Appointment;
import com.tutor.tutorplatform.service.AppointmentService;
import com.tutor.tutorplatform.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.List;

@RestController
@RequestMapping("/api/appointment")
public class AppointmentController extends BaseController {

    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private com.tutor.tutorplatform.service.UserService userService;

    @PostMapping("/create")
    public Result<Appointment> createAppointment(@RequestBody CreateAppointmentDTO dto,
                                                 HttpServletRequest request) {
        Long studentId = getUserIdFromRequest(request);
        Appointment appointment = appointmentService.createAppointment(studentId, dto);
        return Result.success(appointment);
    }

    @GetMapping("/student/list")
    public Result<List<Appointment>> getStudentAppointments(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        Integer role = getRoleFromRequest(request);
        if (role != null && role == 1) {
            return Result.success(appointmentService.getInitiatedAppointments(userId));
        }
        return Result.success(appointmentService.getStudentAppointments(userId));
    }

    @GetMapping("/teacher/list")
    public Result<List<Appointment>> getTeacherAppointments(HttpServletRequest request) {
        Long teacherId = getUserIdFromRequest(request);
        return Result.success(appointmentService.getTeacherAppointments(teacherId));
    }

    @GetMapping("/{id}")
    public Result<Appointment> getAppointment(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        Appointment apt = appointmentService.getById(id);
        if (apt == null) return Result.error("预约不存在");
        if (!userId.equals(apt.getStudentId()) && !userId.equals(apt.getTeacherId())) {
            return Result.error("无权查看");
        }
        Long otherId = userId.equals(apt.getStudentId()) ? apt.getTeacherId() : apt.getStudentId();
        com.tutor.tutorplatform.entity.User other = userService.getById(otherId);
        if (other != null) {
            apt.setOtherPartyId(other.getId());
            apt.setOtherPartyNickname(other.getNickname());
            apt.setOtherPartyAvatar(other.getAvatar());
        }
        return Result.success(apt);
    }

    @PostMapping("/accept/{id}")
    public Result<String> acceptAppointment(@PathVariable Long id, HttpServletRequest request) {
        Long teacherId = getUserIdFromRequest(request);
        boolean ok = appointmentService.acceptAppointment(id, teacherId);
        return ok ? Result.success("接单成功") : Result.error("接单失败");
    }

    @PostMapping("/reject/{id}")
    public Result<String> rejectAppointment(@PathVariable Long id,
                                            @RequestParam(required = false) String reason,
                                            HttpServletRequest request) {
        Long teacherId = getUserIdFromRequest(request);
        boolean ok = appointmentService.rejectAppointment(id, teacherId, reason);
        return ok ? Result.success("已拒绝") : Result.error("拒绝失败");
    }

    @PostMapping("/complete/{id}")
    public Result<String> completeAppointment(@PathVariable Long id, HttpServletRequest request) {
        Long teacherId = getUserIdFromRequest(request);
        boolean success = appointmentService.completeAppointment(id, teacherId);
        return success ? Result.success("授课完成") : Result.error("操作失败");
    }

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
}