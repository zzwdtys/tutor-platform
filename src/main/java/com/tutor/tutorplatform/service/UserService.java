package com.tutor.tutorplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tutor.tutorplatform.entity.User;

public interface UserService extends IService<User> {
    User getByOpenid(String openid);
    User getOrCreateByOpenid(String openid);
    User getByUsername(String username);

    int physicalDeleteById(Long id);

    User getByOpenidIncludeDeleted(String openid);
}