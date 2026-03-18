package com.yzh.campushub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzh.campushub.dto.CreateCommentDTO;
import com.yzh.campushub.dto.Result;
import com.yzh.campushub.entity.Comment;
import com.yzh.campushub.entity.Post;
import com.yzh.campushub.entity.User;
import com.yzh.campushub.mapper.CommentMapper;
import com.yzh.campushub.mapper.PostMapper;
import com.yzh.campushub.mapper.UserMapper;
import com.yzh.campushub.service.CommentService;
import com.yzh.campushub.utils.UserContext;
import com.yzh.campushub.vo.CommentVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result createComment(CreateCommentDTO createCommentDTO) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            userId = 1L; // Default for testing
        }

        // Check if post exists
        Post post = postMapper.selectById(createCommentDTO.getPostId());
        if (post == null || post.getIsDeleted() == 1) {
            return Result.fail("Post not found");
        }

        // Create comment entity
        Comment comment = new Comment();
        BeanUtils.copyProperties(createCommentDTO, comment, "replyUserId");
        
        comment.setUserId(userId);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        comment.setLikeCount(0);
        comment.setStatus(0);
        comment.setIsDeleted(0);
        
        if (createCommentDTO.getParentId() == null) {
            comment.setParentId(0L);
        }

        if (createCommentDTO.getReplyUserId() != null) {
            comment.setReplyUserId(String.valueOf(createCommentDTO.getReplyUserId()));
        }

        // Save comment
        save(comment);

        // Update post comment count safely
        UpdateWrapper<Post> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", post.getId());
        updateWrapper.setSql("comment_count = comment_count + 1");
        postMapper.update(null, updateWrapper);

        return Result.ok();
    }

    @Override
    public Result listPostComments(Long postId) {
        // 1. Check if post exists
        Post post = postMapper.selectById(postId);
        if (post == null || post.getIsDeleted() == 1) {
            return Result.fail("Post not found");
        }

        // 2. Query all comments for the post (not deleted)
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getPostId, postId);
        wrapper.eq(Comment::getIsDeleted, 0);
        wrapper.orderByAsc(Comment::getCreateTime);
        List<Comment> commentList = list(wrapper);

        if (commentList == null || commentList.isEmpty()) {
            return Result.ok(new ArrayList<>());
        }

        // 3. Collect all user IDs involved
        List<Long> userIds = new ArrayList<>();
        commentList.forEach(c -> {
            userIds.add(c.getUserId());
            if (c.getReplyUserId() != null) {
                try {
                    userIds.add(Long.parseLong(c.getReplyUserId()));
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        });
        
        // 4. Fetch user info map
        Map<Long, User> userMap = new java.util.HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds.stream().distinct().collect(Collectors.toList()));
            if (users != null) {
                userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
            }
        }

        // 5. Convert to VO
        Map<Long, User> finalUserMap = userMap;
        List<CommentVO> allVOs = commentList.stream().map(c -> {
            CommentVO vo = new CommentVO();
            BeanUtils.copyProperties(c, vo);
            
            if (c.getReplyUserId() != null) {
                try {
                    vo.setReplyUserId(Long.parseLong(c.getReplyUserId()));
                } catch (NumberFormatException e) {
                    // ignore
                }
            }

            // Fill author info
            User author = finalUserMap.get(c.getUserId());
            if (author != null) {
                vo.setNickname(author.getNickname());
                vo.setAvatar(author.getAvatar());
            }

            // Fill reply user info
            if (vo.getReplyUserId() != null) {
                User replyUser = finalUserMap.get(vo.getReplyUserId());
                if (replyUser != null) {
                    vo.setReplyNickname(replyUser.getNickname());
                }
            }
            return vo;
        }).collect(Collectors.toList());

        // 6. Build tree structure
        // Map by parent ID for faster lookup
        Map<Long, List<CommentVO>> childrenMap = allVOs.stream()
                .filter(c -> c.getParentId() != null && c.getParentId() != 0)
                .collect(Collectors.groupingBy(CommentVO::getParentId));

        // Top level comments (parentId = 0)
        List<CommentVO> rootComments = allVOs.stream()
                .filter(c -> c.getParentId() == null || c.getParentId() == 0)
                .collect(Collectors.toList());
        
        // Assign children
        rootComments.forEach(root -> {
            List<CommentVO> children = childrenMap.getOrDefault(root.getId(), new ArrayList<>());
            root.setChildren(children);
        });

        return Result.ok(rootComments);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteComment(Long commentId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            userId = 1L; // Test user
        }

        Comment comment = getById(commentId);
        if (comment == null || comment.getIsDeleted() == 1) {
            return Result.fail("评论不存在");
        }

        // Check permission (only author can delete)
        if (!comment.getUserId().equals(userId)) {
            return Result.fail("无权删除此评论");
        }

        // Logical delete
        comment.setIsDeleted(1);
        comment.setUpdateTime(LocalDateTime.now());
        updateById(comment);

        // Update post comment count
        UpdateWrapper<Post> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", comment.getPostId());
        updateWrapper.setSql("comment_count = comment_count - 1");
        postMapper.update(null, updateWrapper);

        return Result.ok();
    }
}
