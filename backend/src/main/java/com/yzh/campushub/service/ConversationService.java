package com.yzh.campushub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yzh.campushub.entity.Conversation;

public interface ConversationService extends IService<Conversation> {
    Conversation findOrCreate(Long userAId, Long userBId);
}
