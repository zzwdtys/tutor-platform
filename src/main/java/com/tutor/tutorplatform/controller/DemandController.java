package com.tutor.tutorplatform.controller;

import com.tutor.tutorplatform.common.Result;
import com.tutor.tutorplatform.dto.DemandDTO;
import com.tutor.tutorplatform.entity.Appointment;
import com.tutor.tutorplatform.entity.Demand;
import com.tutor.tutorplatform.entity.Review;
import com.tutor.tutorplatform.service.DemandService;
import com.tutor.tutorplatform.service.ReviewService;
import com.tutor.tutorplatform.service.AppointmentService;
import com.tutor.tutorplatform.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/demand")
public class DemandController extends BaseController{

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private DemandService demandService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/create")
    public Result<Demand> createDemand(@RequestBody DemandDTO demandDTO, HttpServletRequest request) {
        // 从请求头获取 token 并解析 userId
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (demandDTO.getBudgetMin() != null && demandDTO.getBudgetMax() != null
                && demandDTO.getBudgetMin() > demandDTO.getBudgetMax()) {
            return Result.error("预算下限不能大于上限");
        }
        Demand demand = demandService.createDemand(userId, demandDTO);
        return Result.success(demand);
    }

    @GetMapping("/my")
    public Result<List<Demand>> getMyDemands(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        List<Demand> list = demandService.lambdaQuery()
                .eq(Demand::getUserId, userId)
                .orderByDesc(Demand::getCreateTime)
                .list();
        return Result.success(list);
    }

    @PutMapping("/{id}")
    public Result<Demand> updateDemand(@PathVariable Long id, @RequestBody DemandDTO dto, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        Demand demand = demandService.getById(id);
        if (demand == null || !demand.getUserId().equals(userId)) {
            return Result.error("需求不存在或权限不足");
        }
        // 预算校验（提前）
        if (dto.getBudgetMin() != null && dto.getBudgetMax() != null
                && dto.getBudgetMin() > dto.getBudgetMax()) {
            return Result.error("预算下限不能大于上限");
        }
        demand.setSubject(dto.getSubject());
        demand.setGrade(dto.getGrade());
        demand.setLocation(dto.getLocation());
        demand.setBudgetMin(dto.getBudgetMin());
        demand.setBudgetMax(dto.getBudgetMax());
        demand.setTeacherGender(dto.getTeacherGender());
        demand.setDescription(dto.getDescription());
        demandService.updateById(demand);
        return Result.success(demand);
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteDemand(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        Demand demand = demandService.getById(id);
        if (demand == null || !demand.getUserId().equals(userId)) {
            return Result.error("需求不存在或权限不足");
        }
        // 只允许删除状态为“已关闭”的需求（或允许删除任意状态？根据业务决定）
        // 为安全，只删除已关闭的
        if (demand.getStatus() != 3) {
            return Result.error("只能删除已关闭的需求");
        }
        // 级联删除关联的预约和评价
        List<Appointment> appointments = appointmentService.lambdaQuery()
                .eq(Appointment::getDemandId, id)
                .list();
        for (Appointment apt : appointments) {
            reviewService.lambdaUpdate().eq(Review::getAppointmentId, apt.getId()).remove();
        }
        appointmentService.lambdaUpdate().eq(Appointment::getDemandId, id).remove();
        demandService.removeById(id);
        return Result.success("删除成功");
    }

    @GetMapping("/{id}")
    public Result<Demand> getDemandById(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        Demand demand = demandService.getById(id);
        if (demand == null || !demand.getUserId().equals(userId)) {
            return Result.error("需求不存在或权限不足");
        }
        return Result.success(demand);
    }
}