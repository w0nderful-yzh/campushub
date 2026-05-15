package com.yzh.campushub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yzh.campushub.dto.Result;
import com.yzh.campushub.entity.Notice;

public interface NoticeService extends IService<Notice> {
    void createNotice(Long receiveUserId, Long senderUserId, Integer type, Long postId, Long commentId, String content);
    Result listNotices(Integer type, Integer pageNum, Integer pageSize);
    Result getUnreadCount();
    Result markAsRead(Long noticeId);
    Result markAllAsRead();
}
