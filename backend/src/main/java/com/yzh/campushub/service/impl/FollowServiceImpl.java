package com.yzh.campushub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzh.campushub.dto.Result;
import com.yzh.campushub.entity.Follow;
import com.yzh.campushub.entity.User;
import com.yzh.campushub.mapper.FollowMapper;
import com.yzh.campushub.mapper.UserMapper;
import com.yzh.campushub.service.FollowService;
import com.yzh.campushub.service.NoticeService;
import com.yzh.campushub.utils.UserContext;
import com.yzh.campushub.vo.FollowUserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NoticeService noticeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result toggleFollow(Long followUserId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            userId = 1L;
        }

        if (userId.equals(followUserId)) {
            return Result.fail("不能关注自己");
        }

        // Check if target user exists
        User targetUser = userMapper.selectById(followUserId);
        if (targetUser == null) {
            return Result.fail("用户不存在");
        }

        // Check if already following
        LambdaQueryWrapper<Follow> query = new LambdaQueryWrapper<>();
        query.eq(Follow::getUserId, userId);
        query.eq(Follow::getFollowUserId, followUserId);
        Follow existing = getOne(query);

        if (existing != null) {
            // Unfollow
            removeById(existing.getId());
            return Result.ok(false);
        } else {
            // Follow
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            follow.setCreateTime(LocalDateTime.now());
            save(follow);

            User follower = userMapper.selectById(userId);
            String followerName = follower != null ? follower.getNickname() : "someone";
            noticeService.createNotice(followUserId, userId, 3, null, null,
                    followerName + " 关注了你");

            return Result.ok(true);
        }
    }

    @Override
    public Result listFollowing(Integer pageNum, Integer pageSize) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            userId = 1L;
        }

        Page<Follow> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Follow> query = new LambdaQueryWrapper<>();
        query.eq(Follow::getUserId, userId);
        query.orderByDesc(Follow::getCreateTime);
        Page<Follow> resultPage = page(page, query);

        List<Follow> follows = resultPage.getRecords();
        if (follows.isEmpty()) {
            return Result.ok(new ArrayList<>(), 0L);
        }

        List<Long> followUserIds = follows.stream()
                .map(Follow::getFollowUserId)
                .collect(Collectors.toList());

        List<User> users = userMapper.selectBatchIds(followUserIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // Check which users the current user also follows (for mutual follow badge)
        LambdaQueryWrapper<Follow> myFollowQuery = new LambdaQueryWrapper<>();
        myFollowQuery.eq(Follow::getUserId, userId);
        myFollowQuery.in(Follow::getFollowUserId, followUserIds);
        List<Follow> myFollows = list(myFollowQuery);
        Set<Long> myFollowSet = myFollows.stream()
                .map(Follow::getFollowUserId)
                .collect(Collectors.toSet());

        List<FollowUserVO> voList = follows.stream().map(f -> {
            User user = userMap.get(f.getFollowUserId());
            if (user == null) return null;
            FollowUserVO vo = new FollowUserVO();
            vo.setId(user.getId());
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
            vo.setCollege(user.getCollege());
            vo.setIsFollowed(myFollowSet.contains(user.getId()));
            return vo;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        return Result.ok(voList, resultPage.getTotal());
    }

    @Override
    public Result listFollowers(Integer pageNum, Integer pageSize) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            userId = 1L;
        }

        Page<Follow> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Follow> query = new LambdaQueryWrapper<>();
        query.eq(Follow::getFollowUserId, userId);
        query.orderByDesc(Follow::getCreateTime);
        Page<Follow> resultPage = page(page, query);

        List<Follow> follows = resultPage.getRecords();
        if (follows.isEmpty()) {
            return Result.ok(new ArrayList<>(), 0L);
        }

        List<Long> followerIds = follows.stream()
                .map(Follow::getUserId)
                .collect(Collectors.toList());

        List<User> users = userMapper.selectBatchIds(followerIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // Check which followers the current user also follows back
        LambdaQueryWrapper<Follow> myFollowQuery = new LambdaQueryWrapper<>();
        myFollowQuery.eq(Follow::getUserId, userId);
        myFollowQuery.in(Follow::getFollowUserId, followerIds);
        List<Follow> myFollows = list(myFollowQuery);
        Set<Long> myFollowSet = myFollows.stream()
                .map(Follow::getFollowUserId)
                .collect(Collectors.toSet());

        List<FollowUserVO> voList = follows.stream().map(f -> {
            User user = userMap.get(f.getUserId());
            if (user == null) return null;
            FollowUserVO vo = new FollowUserVO();
            vo.setId(user.getId());
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
            vo.setCollege(user.getCollege());
            vo.setIsFollowed(myFollowSet.contains(user.getId()));
            return vo;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        return Result.ok(voList, resultPage.getTotal());
    }

    @Override
    public Result getFollowCount(Long userId) {
        LambdaQueryWrapper<Follow> followingQuery = new LambdaQueryWrapper<>();
        followingQuery.eq(Follow::getUserId, userId);
        long followingCount = count(followingQuery);

        LambdaQueryWrapper<Follow> followerQuery = new LambdaQueryWrapper<>();
        followerQuery.eq(Follow::getFollowUserId, userId);
        long followerCount = count(followerQuery);

        Map<String, Long> countMap = new HashMap<>();
        countMap.put("followingCount", followingCount);
        countMap.put("followerCount", followerCount);

        return Result.ok(countMap);
    }
}
