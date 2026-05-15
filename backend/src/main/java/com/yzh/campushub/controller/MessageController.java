package com.yzh.campushub.controller;

import com.yzh.campushub.dto.Result;
import com.yzh.campushub.dto.SendMessageDTO;
import com.yzh.campushub.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping
    public Result sendMessage(@RequestBody SendMessageDTO dto) {
        return messageService.sendMessage(dto);
    }

    @GetMapping("/conversations")
    public Result listConversations() {
        return messageService.listConversations();
    }

    @GetMapping("/conversations/{conversationId}")
    public Result listMessages(@PathVariable Long conversationId,
                               @RequestParam(defaultValue = "1") Integer pageNum,
                               @RequestParam(defaultValue = "50") Integer pageSize) {
        return messageService.listMessages(conversationId, pageNum, pageSize);
    }

    @GetMapping("/unread-count")
    public Result getUnreadCount() {
        return messageService.getUnreadCount();
    }
}
