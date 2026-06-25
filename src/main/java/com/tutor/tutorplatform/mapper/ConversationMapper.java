package com.tutor.tutorplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tutor.tutorplatform.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
}
