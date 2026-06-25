package com.tutor.tutorplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tutor.tutorplatform.entity.Follow;
import java.util.List;

public interface FollowService extends IService<Follow> {
    /** 切换关注状态（关注/取消关注），返回当前是否已关注 */
    boolean toggleFollow(Long studentId, Long teacherId);
    /** 检查是否已关注 */
    boolean isFollowing(Long studentId, Long teacherId);
    /** 获取学员关注的教员列表（含教员信息） */
    List<Follow> getFollowList(Long studentId);
    /** 获取学员关注数量 */
    Long getFollowCount(Long studentId);
}
