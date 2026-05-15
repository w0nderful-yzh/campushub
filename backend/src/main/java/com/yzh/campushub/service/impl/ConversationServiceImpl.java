package com.yzh.campushub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzh.campushub.entity.Conversation;
import com.yzh.campushub.mapper.ConversationMapper;
import com.yzh.campushub.service.ConversationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements ConversationService {

    @Override
    public Conversation findOrCreate(Long userAId, Long userBId) {
        Long aId = Math.min(userAId, userBId);
        Long bId = Math.max(userAId, userBId);

        LambdaQueryWrapper<Conversation> query = new LambdaQueryWrapper<>();
        query.eq(Conversation::getUserAId, aId);
        query.eq(Conversation::getUserBId, bId);
        Conversation existing = getOne(query);

        if (existing != null) {
            return existing;
        }

        Conversation conv = new Conversation();
        conv.setUserAId(aId);
        conv.setUserBId(bId);
        conv.setCreateTime(LocalDateTime.now());
        conv.setUpdateTime(LocalDateTime.now());
        save(conv);
        return conv;
    }
}
