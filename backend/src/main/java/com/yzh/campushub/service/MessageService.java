package com.yzh.campushub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yzh.campushub.dto.Result;
import com.yzh.campushub.dto.SendMessageDTO;
import com.yzh.campushub.entity.Message;

public interface MessageService extends IService<Message> {
    Result sendMessage(SendMessageDTO dto);
    Result listConversations();
    Result listMessages(Long conversationId, Integer pageNum, Integer pageSize);
    Result getUnreadCount();
}
