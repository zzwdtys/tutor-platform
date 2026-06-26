package com.tutor.tutorplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tutor.tutorplatform.entity.Appointment;
import com.tutor.tutorplatform.entity.Demand;
import com.tutor.tutorplatform.entity.Resume;
import com.tutor.tutorplatform.entity.Review;
import com.tutor.tutorplatform.mapper.AppointmentMapper;
import com.tutor.tutorplatform.mapper.ReviewMapper;
import com.tutor.tutorplatform.entity.User;
import com.tutor.tutorplatform.service.AppointmentService;
import com.tutor.tutorplatform.service.DemandService;
import com.tutor.tutorplatform.service.ResumeService;
import com.tutor.tutorplatform.service.ReviewService;
import com.tutor.tutorplatform.service.UserService;
import com.tutor.tutorplatform.dto.CreateAppointmentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    @Autowired
    private DemandService demandService;
    @Autowired
    private ResumeService resumeService;
    @Autowired
    private ReviewMapper reviewMapper;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private UserService userService;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 填充对方信息
    private void fillOtherParty(List<Appointment> list, boolean fillTeacher) {
        if (list.isEmpty()) return;
        List<Long> userIds = list.stream()
            .map(a -> fillTeacher ? a.getTeacherId() : a.getStudentId())
            .distinct().collect(Collectors.toList());
        List<User> users = userService.listByIds(userIds);
        Map<Long, User> userMap = new HashMap<>();
        for (User u : users) {
            userMap.put(u.getId(), u);
        }
        for (Appointment item : list) {
            Long otherId = fillTeacher ? item.getTeacherId() : item.getStudentId();
            User other = userMap.get(otherId);
            if (other != null) {
                item.setOtherPartyId(other.getId());
                item.setOtherPartyNickname(other.getNickname());
                item.setOtherPartyAvatar(other.getAvatar());
            }
        }
    }

    @Override
    @Transactional
    public Appointment createAppointment(Long userId, CreateAppointmentDTO dto) {
        Demand demand = demandService.getById(dto.getDemandId());
        if (demand == null) {
            throw new RuntimeException("需求不存在");
        }
        Resume resume = resumeService.getById(dto.getResumeId());
        if (resume == null) {
            throw new RuntimeException("教员简历不存在");
        }

        // 判断发起方：学员发起（用户是需求所有者）还是教员发起（用户是简历所有者）
        boolean isStudent = demand.getUserId().equals(userId);
        boolean isTeacher = resume.getUserId().equals(userId);
        if (!isStudent && !isTeacher) {
            throw new RuntimeException("权限不足：您既不是该需求的发布者，也不是该简历的所有者");
        }

        Long studentId = demand.getUserId();
        Long teacherId = resume.getUserId();
        if (studentId.equals(teacherId)) {
            throw new RuntimeException("不能预约自己");
        }

        Appointment appointment = new Appointment();
        appointment.setDemandId(dto.getDemandId());
        appointment.setResumeId(dto.getResumeId());
        appointment.setStudentId(studentId);
        appointment.setTeacherId(teacherId);
        appointment.setStatus(0);
        // 记录发起方：学员发起=0，教员从需求广场发起=1
        appointment.setInitiator(isTeacher ? 1 : 0);
        String timeStr = dto.getAppointmentTime();
        if (timeStr != null && timeStr.length() == 10) {
            timeStr = timeStr + " 12:00:00";
        }
        try {
            appointment.setAppointmentTime(sdf.parse(timeStr));
        } catch (ParseException e) {
            throw new RuntimeException("时间格式错误，请使用 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss");
        }
        this.save(appointment);
        return appointment;
    }

    @Override
    public List<Appointment> getStudentAppointments(Long studentId) {
        List<Appointment> list = lambdaQuery()
                .eq(Appointment::getStudentId, studentId)
                .eq(Appointment::getInitiator, 0)  // 学员发起的
                .orderByDesc(Appointment::getCreateTime)
                .list();
        for (Appointment item : list) {
            // 使用 ReviewMapper 查询是否存在评价记录
            LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Review::getAppointmentId, item.getId());
            Integer count = Math.toIntExact(reviewMapper.selectCount(wrapper));
            item.setReviewed(count != null && count > 0);
            Review review = reviewService.lambdaQuery()
                    .eq(Review::getAppointmentId,item.getId())
                    .one();
            item.setReview(review);
        }
        fillOtherParty(list, true); // 对方是教员
        return list;
    }

    @Override
    public List<Appointment> getInitiatedAppointments(Long teacherId) {
        List<Appointment> list = lambdaQuery()
                .eq(Appointment::getTeacherId, teacherId)
                .eq(Appointment::getInitiator, 1)  // 教员主动发起的
                .orderByDesc(Appointment::getCreateTime)
                .list();
        for (Appointment item : list) {
            LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Review::getAppointmentId, item.getId());
            Integer count = Math.toIntExact(reviewMapper.selectCount(wrapper));
            item.setReviewed(count != null && count > 0);
            Review review = reviewService.lambdaQuery()
                    .eq(Review::getAppointmentId, item.getId())
                    .one();
            item.setReview(review);
        }
        fillOtherParty(list, false); // 对方是学员
        return list;
    }

    @Override
    public List<Appointment> getTeacherAppointments(Long teacherId) {
        List<Appointment> list = lambdaQuery()
                .eq(Appointment::getTeacherId, teacherId)
                .eq(Appointment::getInitiator, 0)  // 学员发起的（教员收到的）
                .orderByDesc(Appointment::getCreateTime)
                .list();
        fillOtherParty(list, false); // 对方是学员
        return list;
    }

    @Override
    @Transactional
    public boolean acceptAppointment(Long appointmentId, Long teacherId) {
        Appointment appointment = getById(appointmentId);
        if (appointment == null || !appointment.getTeacherId().equals(teacherId)) {
            return false;
        }
        if (appointment.getStatus() != 0) {
            return false;
        }
        appointment.setStatus(1);
        demandService.lambdaUpdate()
                .eq(Demand::getId, appointment.getDemandId())
                .set(Demand::getStatus, 1)
                .update();
        return updateById(appointment);
    }

    @Override
    @Transactional
    public boolean rejectAppointment(Long appointmentId, Long teacherId, String reason) {
        Appointment appointment = getById(appointmentId);
        if (appointment == null || !appointment.getTeacherId().equals(teacherId)) {
            return false;
        }
        if (appointment.getStatus() != 0) {
            return false;
        }
        appointment.setStatus(2);
        if (reason != null && !reason.isEmpty()) {
            appointment.setRejectReason(reason);
        }
        return updateById(appointment);
    }

    @Override
    @Transactional
    public boolean completeAppointment(Long appointmentId, Long teacherId) {
        Appointment appointment = getById(appointmentId);
        if (appointment == null || !appointment.getTeacherId().equals(teacherId)) {
            return false;
        }
        if (appointment.getStatus() != 1) {
            return false;
        }
        appointment.setStatus(3);
        return updateById(appointment);
    }
}