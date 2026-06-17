package com.tutor.tutorplatform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tutor.tutorplatform.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Delete("DELETE FROM `user` WHERE id = #{id}")
    int physicalDeleteById(Long id);

    @Select("SELECT * FROM `user` WHERE openid = #{openid} LIMIT 1")
    User selectByOpenidIncludeDeleted(String openid);

}