package com.yzh.campushub.controller;

import com.yzh.campushub.dto.Result;
import com.yzh.campushub.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @GetMapping
    public Result listNotices(@RequestParam(required = false) Integer type,
                              @RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        return noticeService.listNotices(type, pageNum, pageSize);
    }

    @GetMapping("/unread-count")
    public Result getUnreadCount() {
        return noticeService.getUnreadCount();
    }

    @PutMapping("/{noticeId}/read")
    public Result markAsRead(@PathVariable Long noticeId) {
        return noticeService.markAsRead(noticeId);
    }

    @PutMapping("/read-all")
    public Result markAllAsRead() {
        return noticeService.markAllAsRead();
    }
}
