package com.tutor.tutorplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tutor.tutorplatform.entity.Appointment;
import com.tutor.tutorplatform.dto.CreateAppointmentDTO;

import java.util.List;

public interface AppointmentService extends IService<Appointment> {
    Appointment createAppointment(Long studentId, CreateAppointmentDTO dto);
    List<Appointment> getStudentAppointments(Long studentId);
    List<Appointment> getTeacherAppointments(Long teacherId);
    /** 教员从需求广场主动发起的预约 */
    List<Appointment> getInitiatedAppointments(Long teacherId);
    boolean acceptAppointment(Long appointmentId, Long teacherId);
    boolean rejectAppointment(Long appointmentId, Long teacherId, String reason);
    boolean completeAppointment(Long appointmentId, Long teacherId);
}