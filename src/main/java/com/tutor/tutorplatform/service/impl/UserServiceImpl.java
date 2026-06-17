package com.tutor.tutorplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tutor.tutorplatform.entity.User;
import com.tutor.tutorplatform.mapper.UserMapper;
import com.tutor.tutorplatform.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User getByOpenid(String openid) {
        return this.getOne(new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));
    }

    @Override
    public User getOrCreateByOpenid(String openid) {
        User user = this.getByOpenid(openid);
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setRole(0);      // 默认学员
            user.setStatus(1);    // 正常
            this.save(user);
            log.info("新用户注册: openid={}, userId={}", openid, user.getId());
        }
        return user;
    }

    @Override
    public User getByUsername(String username) {
        return lambdaQuery()
                .eq(User::getUsername, username)
                .eq(User::getIsDeleted, 0)  // 只查未删除的
                .one();
    }

    @Override
    public User getByOpenidIncludeDeleted(String openid) {
        return baseMapper.selectByOpenidIncludeDeleted(openid);
    }

    @Override
    public int physicalDeleteById(Long id) {
        return baseMapper.physicalDeleteById(id);
    }
}