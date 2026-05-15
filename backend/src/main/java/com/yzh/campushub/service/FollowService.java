package com.yzh.campushub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yzh.campushub.dto.Result;
import com.yzh.campushub.entity.Follow;

public interface FollowService extends IService<Follow> {
    Result toggleFollow(Long followUserId);
    Result listFollowing(Integer pageNum, Integer pageSize);
    Result listFollowers(Integer pageNum, Integer pageSize);
    Result getFollowCount(Long userId);
}
