package com.tutor.tutorplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tutor.tutorplatform.entity.Demand;
import com.tutor.tutorplatform.dto.DemandDTO;

public interface DemandService extends IService<Demand> {
    Demand createDemand(Long userId, DemandDTO demandDTO);
}