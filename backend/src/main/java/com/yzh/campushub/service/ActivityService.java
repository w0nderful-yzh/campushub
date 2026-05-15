package com.yzh.campushub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yzh.campushub.dto.CreateActivityDTO;
import com.yzh.campushub.dto.Result;
import com.yzh.campushub.entity.Activity;

public interface ActivityService extends IService<Activity> {
    Result createActivity(CreateActivityDTO dto);
    Result listActivities(Integer activityType, String keyword, Integer pageNum, Integer pageSize);
    Result getActivityDetail(Long id);
    Result updateActivity(Long id, CreateActivityDTO dto);
    Result cancelActivity(Long id);
    Result signup(Long activityId);
    Result cancelSignup(Long activityId);
    Result listSignups(Long activityId, Integer pageNum, Integer pageSize);
    Result listMyActivities(Integer pageNum, Integer pageSize);
    Result listMySignups(Integer pageNum, Integer pageSize);
}
