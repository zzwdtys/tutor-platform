package com.tutor.tutorplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tutor.tutorplatform.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
