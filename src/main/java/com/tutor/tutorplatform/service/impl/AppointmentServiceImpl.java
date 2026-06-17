package com.tutor.tutorplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tutor.tutorplatform.entity.Appointment;
import com.tutor.tutorplatform.entity.Demand;
import com.tutor.tutorplatform.entity.Resume;
import com.tutor.tutorplatform.entity.Review;
import com.tutor.tutorplatform.mapper.AppointmentMapper;
import com.tutor.tutorplatform.mapper.ReviewMapper;
import com.tutor.tutorplatform.service.AppointmentService;
import com.tutor.tutorplatform.service.DemandService;
import com.tutor.tutorplatform.service.ResumeService;
import com.tutor.tutorplatform.service.ReviewService;
import com.tutor.tutorplatform.dto.CreateAppointmentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

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

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public Appointment createAppointment(Long studentId, CreateAppointmentDTO dto) {
        // ... 保持不变 ...
        Demand demand = demandService.getById(dto.getDemandId());
        if (demand == null || !demand.getUserId().equals(studentId)) {
            throw new RuntimeException("需求不存在或权限不足");
        }
        Resume resume = resumeService.getById(dto.getResumeId());
        if (resume == null) {
            throw new RuntimeException("教员简历不存在");
        }

        Appointment appointment = new Appointment();
        appointment.setDemandId(dto.getDemandId());
        appointment.setResumeId(dto.getResumeId());
        appointment.setStudentId(studentId);
        appointment.setTeacherId(resume.getUserId());
        appointment.setStatus(0);
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
        return list;
    }

    @Override
    public List<Appointment> getTeacherAppointments(Long teacherId) {
        return lambdaQuery()
                .eq(Appointment::getTeacherId, teacherId)
                .orderByDesc(Appointment::getCreateTime)
                .list();
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