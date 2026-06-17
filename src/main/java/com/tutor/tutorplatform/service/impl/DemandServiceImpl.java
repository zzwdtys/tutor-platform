package com.tutor.tutorplatform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tutor.tutorplatform.entity.Demand;
import com.tutor.tutorplatform.mapper.DemandMapper;
import com.tutor.tutorplatform.service.DemandService;
import com.tutor.tutorplatform.dto.DemandDTO;
import org.springframework.stereotype.Service;

@Service
public class DemandServiceImpl extends ServiceImpl<DemandMapper, Demand> implements DemandService {
    @Override
    public Demand createDemand(Long userId, DemandDTO dto) {
        Demand demand = new Demand();
        demand.setUserId(userId);
        demand.setSubject(dto.getSubject());
        demand.setGrade(dto.getGrade());
        demand.setLocation(dto.getLocation());
        demand.setBudgetMin(dto.getBudgetMin());
        demand.setBudgetMax(dto.getBudgetMax());
        demand.setTeacherGender(dto.getTeacherGender() == null ? 0 : dto.getTeacherGender());
        demand.setDescription(dto.getDescription());
        demand.setStatus(0); // 0-进行中
        this.save(demand);
        return demand;
    }
}