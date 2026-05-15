package com.yzh.campushub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzh.campushub.dto.CreateActivityDTO;
import com.yzh.campushub.dto.Result;
import com.yzh.campushub.entity.Activity;
import com.yzh.campushub.entity.ActivitySignup;
import com.yzh.campushub.entity.User;
import com.yzh.campushub.mapper.ActivityMapper;
import com.yzh.campushub.mapper.ActivitySignupMapper;
import com.yzh.campushub.mapper.UserMapper;
import com.yzh.campushub.service.ActivityService;
import com.yzh.campushub.utils.UserContext;
import com.yzh.campushub.vo.ActivitySignupVO;
import com.yzh.campushub.vo.ActivityVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {

    @Autowired
    private ActivitySignupMapper signupMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Result createActivity(CreateActivityDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) userId = 1L;

        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            return Result.fail("活动标题不能为空");
        }
        if (dto.getStartTime() == null) {
            return Result.fail("开始时间不能为空");
        }

        Activity activity = new Activity();
        BeanUtils.copyProperties(dto, activity);
        activity.setUserId(userId);
        activity.setCurrentCount(0);
        activity.setStatus(1);
        activity.setIsDeleted(0);
        activity.setCreateTime(LocalDateTime.now());
        activity.setUpdateTime(LocalDateTime.now());
        if (activity.getMaxParticipants() == null) activity.setMaxParticipants(0);
        save(activity);

        return Result.ok(activity.getId());
    }

    @Override
    public Result listActivities(Integer activityType, String keyword, Integer pageNum, Integer pageSize) {
        Long userId = UserContext.getUserId();

        Page<Activity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Activity> query = new LambdaQueryWrapper<>();
        query.eq(Activity::getIsDeleted, 0);
        if (activityType != null) {
            query.eq(Activity::getActivityType, activityType);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            query.and(q -> q.like(Activity::getTitle, keyword).or().like(Activity::getDescription, keyword));
        }
        query.orderByAsc(Activity::getStartTime);
        Page<Activity> resultPage = page(page, query);

        List<Activity> activities = resultPage.getRecords();
        if (activities.isEmpty()) {
            return Result.ok(new ArrayList<>(), 0L);
        }

        List<Long> authorIds = activities.stream().map(Activity::getUserId).distinct().collect(Collectors.toList());
        List<User> authors = userMapper.selectBatchIds(authorIds);
        Map<Long, User> authorMap = authors.stream().collect(Collectors.toMap(User::getId, u -> u));

        // Check signup status
        Set<Long> signedUpIds = new HashSet<>();
        if (userId != null) {
            List<Long> activityIds = activities.stream().map(Activity::getId).collect(Collectors.toList());
            LambdaQueryWrapper<ActivitySignup> sq = new LambdaQueryWrapper<>();
            sq.eq(ActivitySignup::getUserId, userId);
            sq.in(ActivitySignup::getActivityId, activityIds);
            sq.eq(ActivitySignup::getStatus, 1);
            List<ActivitySignup> signups = signupMapper.selectList(sq);
            signedUpIds = signups.stream().map(ActivitySignup::getActivityId).collect(Collectors.toSet());
        }

        Set<Long> finalSignedUpIds = signedUpIds;
        List<ActivityVO> voList = activities.stream().map(a -> {
            ActivityVO vo = new ActivityVO();
            BeanUtils.copyProperties(a, vo);
            User author = authorMap.get(a.getUserId());
            if (author != null) {
                vo.setAuthorNickname(author.getNickname());
                vo.setAuthorAvatar(author.getAvatar());
            }
            vo.setIsSignedUp(finalSignedUpIds.contains(a.getId()));
            return vo;
        }).collect(Collectors.toList());

        return Result.ok(voList, resultPage.getTotal());
    }

    @Override
    public Result getActivityDetail(Long id) {
        Long userId = UserContext.getUserId();

        Activity activity = getById(id);
        if (activity == null || activity.getIsDeleted() == 1) {
            return Result.fail("活动不存在");
        }

        ActivityVO vo = new ActivityVO();
        BeanUtils.copyProperties(activity, vo);

        User author = userMapper.selectById(activity.getUserId());
        if (author != null) {
            vo.setAuthorNickname(author.getNickname());
            vo.setAuthorAvatar(author.getAvatar());
        }

        if (userId != null) {
            LambdaQueryWrapper<ActivitySignup> sq = new LambdaQueryWrapper<>();
            sq.eq(ActivitySignup::getActivityId, id);
            sq.eq(ActivitySignup::getUserId, userId);
            sq.eq(ActivitySignup::getStatus, 1);
            vo.setIsSignedUp(signupMapper.selectCount(sq) > 0);
        }

        return Result.ok(vo);
    }

    @Override
    public Result updateActivity(Long id, CreateActivityDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) userId = 1L;

        Activity activity = getById(id);
        if (activity == null || activity.getIsDeleted() == 1) {
            return Result.fail("活动不存在");
        }
        if (!activity.getUserId().equals(userId)) {
            return Result.fail("无权编辑此活动");
        }

        if (dto.getTitle() != null) activity.setTitle(dto.getTitle());
        if (dto.getDescription() != null) activity.setDescription(dto.getDescription());
        if (dto.getCoverImg() != null) activity.setCoverImg(dto.getCoverImg());
        if (dto.getLocation() != null) activity.setLocation(dto.getLocation());
        if (dto.getActivityType() != null) activity.setActivityType(dto.getActivityType());
        if (dto.getStartTime() != null) activity.setStartTime(dto.getStartTime());
        if (dto.getEndTime() != null) activity.setEndTime(dto.getEndTime());
        if (dto.getMaxParticipants() != null) activity.setMaxParticipants(dto.getMaxParticipants());
        activity.setUpdateTime(LocalDateTime.now());
        updateById(activity);

        return Result.ok();
    }

    @Override
    public Result cancelActivity(Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) userId = 1L;

        Activity activity = getById(id);
        if (activity == null || activity.getIsDeleted() == 1) {
            return Result.fail("活动不存在");
        }
        if (!activity.getUserId().equals(userId)) {
            return Result.fail("无权取消此活动");
        }

        activity.setStatus(0);
        activity.setUpdateTime(LocalDateTime.now());
        updateById(activity);

        return Result.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result signup(Long activityId) {
        Long userId = UserContext.getUserId();
        if (userId == null) userId = 1L;

        Activity activity = getById(activityId);
        if (activity == null || activity.getIsDeleted() == 1) {
            return Result.fail("活动不存在");
        }
        if (activity.getStatus() != 1) {
            return Result.fail("活动已结束或已取消");
        }

        // Check duplicate
        LambdaQueryWrapper<ActivitySignup> dq = new LambdaQueryWrapper<>();
        dq.eq(ActivitySignup::getActivityId, activityId);
        dq.eq(ActivitySignup::getUserId, userId);
        dq.eq(ActivitySignup::getStatus, 1);
        if (signupMapper.selectCount(dq) > 0) {
            return Result.fail("你已报名该活动");
        }

        // Check capacity
        if (activity.getMaxParticipants() > 0 && activity.getCurrentCount() >= activity.getMaxParticipants()) {
            return Result.fail("报名人数已满");
        }

        ActivitySignup signup = new ActivitySignup();
        signup.setActivityId(activityId);
        signup.setUserId(userId);
        signup.setStatus(1);
        signup.setCreateTime(LocalDateTime.now());
        signupMapper.insert(signup);

        UpdateWrapper<Activity> uw = new UpdateWrapper<>();
        uw.eq("id", activityId);
        uw.setSql("current_count = current_count + 1");
        update(null, uw);

        return Result.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result cancelSignup(Long activityId) {
        Long userId = UserContext.getUserId();
        if (userId == null) userId = 1L;

        LambdaQueryWrapper<ActivitySignup> sq = new LambdaQueryWrapper<>();
        sq.eq(ActivitySignup::getActivityId, activityId);
        sq.eq(ActivitySignup::getUserId, userId);
        sq.eq(ActivitySignup::getStatus, 1);
        ActivitySignup signup = signupMapper.selectOne(sq);

        if (signup == null) {
            return Result.fail("你尚未报名该活动");
        }

        signup.setStatus(0);
        signupMapper.updateById(signup);

        UpdateWrapper<Activity> uw = new UpdateWrapper<>();
        uw.eq("id", activityId);
        uw.setSql("current_count = current_count - 1");
        update(null, uw);

        return Result.ok();
    }

    @Override
    public Result listSignups(Long activityId, Integer pageNum, Integer pageSize) {
        Page<ActivitySignup> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ActivitySignup> query = new LambdaQueryWrapper<>();
        query.eq(ActivitySignup::getActivityId, activityId);
        query.eq(ActivitySignup::getStatus, 1);
        query.orderByDesc(ActivitySignup::getCreateTime);
        Page<ActivitySignup> resultPage = signupMapper.selectPage(page, query);

        List<ActivitySignup> signups = resultPage.getRecords();
        if (signups.isEmpty()) {
            return Result.ok(new ArrayList<>(), 0L);
        }

        List<Long> userIds = signups.stream().map(ActivitySignup::getUserId).collect(Collectors.toList());
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));

        List<ActivitySignupVO> voList = signups.stream().map(s -> {
            ActivitySignupVO vo = new ActivitySignupVO();
            vo.setUserId(s.getUserId());
            vo.setSignupTime(s.getCreateTime());
            User user = userMap.get(s.getUserId());
            if (user != null) {
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatar());
            }
            return vo;
        }).collect(Collectors.toList());

        return Result.ok(voList, resultPage.getTotal());
    }

    @Override
    public Result listMyActivities(Integer pageNum, Integer pageSize) {
        Long userId = UserContext.getUserId();
        if (userId == null) userId = 1L;

        Page<Activity> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Activity> query = new LambdaQueryWrapper<>();
        query.eq(Activity::getUserId, userId);
        query.eq(Activity::getIsDeleted, 0);
        query.orderByDesc(Activity::getCreateTime);
        Page<Activity> resultPage = page(page, query);

        List<ActivityVO> voList = resultPage.getRecords().stream().map(a -> {
            ActivityVO vo = new ActivityVO();
            BeanUtils.copyProperties(a, vo);
            vo.setIsSignedUp(false);
            return vo;
        }).collect(Collectors.toList());

        return Result.ok(voList, resultPage.getTotal());
    }

    @Override
    public Result listMySignups(Integer pageNum, Integer pageSize) {
        Long userId = UserContext.getUserId();
        if (userId == null) userId = 1L;

        Page<ActivitySignup> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ActivitySignup> sq = new LambdaQueryWrapper<>();
        sq.eq(ActivitySignup::getUserId, userId);
        sq.eq(ActivitySignup::getStatus, 1);
        sq.orderByDesc(ActivitySignup::getCreateTime);
        Page<ActivitySignup> signupPage = signupMapper.selectPage(page, sq);

        List<ActivitySignup> signups = signupPage.getRecords();
        if (signups.isEmpty()) {
            return Result.ok(new ArrayList<>(), 0L);
        }

        List<Long> activityIds = signups.stream().map(ActivitySignup::getActivityId).collect(Collectors.toList());
        List<Activity> activities = listByIds(activityIds);

        List<ActivityVO> voList = activities.stream().map(a -> {
            ActivityVO vo = new ActivityVO();
            BeanUtils.copyProperties(a, vo);
            User author = userMapper.selectById(a.getUserId());
            if (author != null) {
                vo.setAuthorNickname(author.getNickname());
                vo.setAuthorAvatar(author.getAvatar());
            }
            vo.setIsSignedUp(true);
            return vo;
        }).collect(Collectors.toList());

        return Result.ok(voList, signupPage.getTotal());
    }
}
