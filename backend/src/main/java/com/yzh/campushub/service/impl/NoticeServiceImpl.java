package com.yzh.campushub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzh.campushub.dto.Result;
import com.yzh.campushub.entity.Notice;
import com.yzh.campushub.entity.User;
import com.yzh.campushub.mapper.NoticeMapper;
import com.yzh.campushub.mapper.UserMapper;
import com.yzh.campushub.service.NoticeService;
import com.yzh.campushub.utils.UserContext;
import com.yzh.campushub.vo.NoticeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public void createNotice(Long receiveUserId, Long senderUserId, Integer type,
                             Long postId, Long commentId, String content) {
        if (receiveUserId.equals(senderUserId)) return;

        Notice notice = new Notice();
        notice.setReceiveUserId(receiveUserId);
        notice.setSenderUserId(senderUserId);
        notice.setType(type);
        notice.setPostId(postId);
        notice.setCommentId(commentId);
        notice.setContent(content);
        notice.setIsRead(0);
        notice.setCreateTime(LocalDateTime.now());
        save(notice);
    }

    @Override
    public Result listNotices(Integer type, Integer pageNum, Integer pageSize) {
        Long userId = UserContext.getUserId();
        if (userId == null) userId = 1L;

        Page<Notice> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notice> query = new LambdaQueryWrapper<>();
        query.eq(Notice::getReceiveUserId, userId);
        if (type != null) {
            query.eq(Notice::getType, type);
        }
        query.orderByDesc(Notice::getCreateTime);
        Page<Notice> resultPage = page(page, query);

        List<Notice> notices = resultPage.getRecords();
        if (notices.isEmpty()) {
            return Result.ok(new ArrayList<>(), 0L);
        }

        List<Long> senderIds = notices.stream()
                .map(Notice::getSenderUserId)
                .distinct()
                .collect(Collectors.toList());

        List<User> senders = userMapper.selectBatchIds(senderIds);
        Map<Long, User> senderMap = senders.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<NoticeVO> voList = notices.stream().map(n -> {
            NoticeVO vo = new NoticeVO();
            vo.setId(n.getId());
            vo.setType(n.getType());
            vo.setContent(n.getContent());
            vo.setSenderUserId(n.getSenderUserId());
            vo.setPostId(n.getPostId());
            vo.setCommentId(n.getCommentId());
            vo.setIsRead(n.getIsRead());
            vo.setCreateTime(n.getCreateTime());

            User sender = senderMap.get(n.getSenderUserId());
            if (sender != null) {
                vo.setSenderNickname(sender.getNickname());
                vo.setSenderAvatar(sender.getAvatar());
            }
            return vo;
        }).collect(Collectors.toList());

        return Result.ok(voList, resultPage.getTotal());
    }

    @Override
    public Result getUnreadCount() {
        Long userId = UserContext.getUserId();
        if (userId == null) userId = 1L;

        LambdaQueryWrapper<Notice> query = new LambdaQueryWrapper<>();
        query.eq(Notice::getReceiveUserId, userId);
        query.eq(Notice::getIsRead, 0);
        long count = count(query);

        return Result.ok(count);
    }

    @Override
    public Result markAsRead(Long noticeId) {
        Long userId = UserContext.getUserId();
        if (userId == null) userId = 1L;

        LambdaUpdateWrapper<Notice> update = new LambdaUpdateWrapper<>();
        update.eq(Notice::getId, noticeId);
        update.eq(Notice::getReceiveUserId, userId);
        update.set(Notice::getIsRead, 1);
        update(update);

        return Result.ok();
    }

    @Override
    public Result markAllAsRead() {
        Long userId = UserContext.getUserId();
        if (userId == null) userId = 1L;

        LambdaUpdateWrapper<Notice> update = new LambdaUpdateWrapper<>();
        update.eq(Notice::getReceiveUserId, userId);
        update.eq(Notice::getIsRead, 0);
        update.set(Notice::getIsRead, 1);
        update(update);

        return Result.ok();
    }
}
