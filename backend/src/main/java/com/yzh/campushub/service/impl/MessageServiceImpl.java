package com.yzh.campushub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzh.campushub.dto.Result;
import com.yzh.campushub.dto.SendMessageDTO;
import com.yzh.campushub.entity.Conversation;
import com.yzh.campushub.entity.Message;
import com.yzh.campushub.entity.User;
import com.yzh.campushub.mapper.MessageMapper;
import com.yzh.campushub.mapper.UserMapper;
import com.yzh.campushub.service.ConversationService;
import com.yzh.campushub.service.MessageService;
import com.yzh.campushub.utils.UserContext;
import com.yzh.campushub.vo.ConversationVO;
import com.yzh.campushub.vo.MessageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result sendMessage(SendMessageDTO dto) {
        Long userId = UserContext.getUserId();
        if (userId == null) userId = 1L;

        if (dto.getReceiverId() == null || dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            return Result.fail("参数不完整");
        }
        if (userId.equals(dto.getReceiverId())) {
            return Result.fail("不能给自己发消息");
        }

        User receiver = userMapper.selectById(dto.getReceiverId());
        if (receiver == null) {
            return Result.fail("用户不存在");
        }

        Conversation conv = conversationService.findOrCreate(userId, dto.getReceiverId());

        Message msg = new Message();
        msg.setConversationId(conv.getId());
        msg.setSenderId(userId);
        msg.setContent(dto.getContent().trim());
        msg.setIsRead(0);
        msg.setCreateTime(LocalDateTime.now());
        save(msg);

        // Update conversation
        conv.setLastMessageId(msg.getId());
        conv.setLastMessageTime(msg.getCreateTime());
        conv.setUpdateTime(LocalDateTime.now());
        conversationService.updateById(conv);

        return Result.ok();
    }

    @Override
    public Result listConversations() {
        final Long currentUserId = UserContext.getUserId() != null ? UserContext.getUserId() : 1L;

        LambdaQueryWrapper<Conversation> query = new LambdaQueryWrapper<>();
        query.and(q -> q.eq(Conversation::getUserAId, currentUserId).or().eq(Conversation::getUserBId, currentUserId));
        query.orderByDesc(Conversation::getLastMessageTime);
        List<Conversation> conversations = conversationService.list(query);

        if (conversations.isEmpty()) {
            return Result.ok(new ArrayList<>());
        }

        List<Long> otherUserIds = conversations.stream()
                .map(c -> c.getUserAId().equals(currentUserId) ? c.getUserBId() : c.getUserAId())
                .collect(Collectors.toList());

        List<User> users = userMapper.selectBatchIds(otherUserIds);
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));

        List<ConversationVO> voList = conversations.stream().map(c -> {
            Long otherId = c.getUserAId().equals(currentUserId) ? c.getUserBId() : c.getUserAId();
            User other = userMap.get(otherId);

            ConversationVO vo = new ConversationVO();
            vo.setId(c.getId());
            vo.setOtherUserId(otherId);
            vo.setOtherNickname(other != null ? other.getNickname() : "未知用户");
            vo.setOtherAvatar(other != null ? other.getAvatar() : null);
            vo.setLastMessageTime(c.getLastMessageTime());

            if (c.getLastMessageId() != null) {
                Message lastMsg = getById(c.getLastMessageId());
                if (lastMsg != null) vo.setLastMessage(lastMsg.getContent());
            }

            LambdaQueryWrapper<Message> unreadQuery = new LambdaQueryWrapper<>();
            unreadQuery.eq(Message::getConversationId, c.getId());
            unreadQuery.eq(Message::getIsRead, 0);
            unreadQuery.ne(Message::getSenderId, currentUserId);
            vo.setUnreadCount((int) count(unreadQuery));

            return vo;
        }).collect(Collectors.toList());

        return Result.ok(voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result listMessages(Long conversationId, Integer pageNum, Integer pageSize) {
        final Long currentUserId = UserContext.getUserId() != null ? UserContext.getUserId() : 1L;

        LambdaUpdateWrapper<Message> update = new LambdaUpdateWrapper<>();
        update.eq(Message::getConversationId, conversationId);
        update.eq(Message::getIsRead, 0);
        update.ne(Message::getSenderId, currentUserId);
        update.set(Message::getIsRead, 1);
        update(update);

        Page<Message> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Message> query = new LambdaQueryWrapper<>();
        query.eq(Message::getConversationId, conversationId);
        query.orderByDesc(Message::getCreateTime);
        Page<Message> resultPage = page(page, query);

        List<Message> messages = resultPage.getRecords();
        if (messages.isEmpty()) {
            return Result.ok(new ArrayList<>(), 0L);
        }

        List<Long> senderIds = messages.stream().map(Message::getSenderId).distinct().collect(Collectors.toList());
        List<User> senders = userMapper.selectBatchIds(senderIds);
        Map<Long, User> senderMap = senders.stream().collect(Collectors.toMap(User::getId, u -> u));

        List<MessageVO> voList = messages.stream().map(m -> {
            MessageVO vo = new MessageVO();
            vo.setId(m.getId());
            vo.setSenderId(m.getSenderId());
            vo.setContent(m.getContent());
            vo.setIsRead(m.getIsRead());
            vo.setCreateTime(m.getCreateTime());

            User sender = senderMap.get(m.getSenderId());
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
        final Long currentUserId = UserContext.getUserId() != null ? UserContext.getUserId() : 1L;

        LambdaQueryWrapper<Conversation> convQuery = new LambdaQueryWrapper<>();
        convQuery.and(q -> q.eq(Conversation::getUserAId, currentUserId).or().eq(Conversation::getUserBId, currentUserId));
        List<Conversation> conversations = conversationService.list(convQuery);

        if (conversations.isEmpty()) {
            return Result.ok(0);
        }

        List<Long> convIds = conversations.stream().map(Conversation::getId).collect(Collectors.toList());

        LambdaQueryWrapper<Message> query = new LambdaQueryWrapper<>();
        query.in(Message::getConversationId, convIds);
        query.eq(Message::getIsRead, 0);
        query.ne(Message::getSenderId, currentUserId);
        long count = count(query);

        return Result.ok(count);
    }
}
