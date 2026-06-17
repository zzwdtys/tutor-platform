package com.tutor.tutorplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tutor.tutorplatform.entity.Review;
import com.tutor.tutorplatform.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {
}