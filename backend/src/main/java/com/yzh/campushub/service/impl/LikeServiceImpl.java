package com.yzh.campushub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzh.campushub.dto.Result;
import com.yzh.campushub.entity.Like;
import com.yzh.campushub.entity.Post;
import com.yzh.campushub.mapper.LikeMapper;
import com.yzh.campushub.mapper.PostMapper;
import com.yzh.campushub.service.LikeService;
import com.yzh.campushub.utils.UserContext;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yzh.campushub.dto.PostQueryDTO;
import com.yzh.campushub.vo.PostVO;
import com.yzh.campushub.entity.Category;
import com.yzh.campushub.entity.User;
import com.yzh.campushub.mapper.CategoryMapper;
import com.yzh.campushub.mapper.UserMapper;
import org.springframework.beans.BeanUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class LikeServiceImpl extends ServiceImpl<LikeMapper, Like> implements LikeService {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result likePost(Long postId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            userId = 1L; // Test user
        }

        // Check if post exists
        Post post = postMapper.selectById(postId);
        if (post == null || post.getIsDeleted() == 1) {
            return Result.fail("Post not found");
        }

        // Check if already liked
        LambdaQueryWrapper<Like> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Like::getPostId, postId);
        queryWrapper.eq(Like::getUserId, userId);
        Like existingLike = getOne(queryWrapper);

        UpdateWrapper<Post> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", postId);

        if (existingLike != null) {
            // Already liked, so unlike
            removeById(existingLike.getId());
            // Decrement like count
            updateWrapper.setSql("like_count = like_count - 1");
        } else {
            // Not liked, so like
            Like newLike = new Like();
            newLike.setPostId(postId);
            newLike.setUserId(userId);
            newLike.setCreateTime(LocalDateTime.now());
            save(newLike);
            // Increment like count
            updateWrapper.setSql("like_count = like_count + 1");
        }
        
        postMapper.update(null, updateWrapper);
        
        return Result.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result unlikePost(Long postId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            userId = 1L; // Test user
        }

        // Check if post exists
        Post post = postMapper.selectById(postId);
        if (post == null || post.getIsDeleted() == 1) {
            return Result.fail("Post not found");
        }

        // Check if already liked
        LambdaQueryWrapper<Like> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Like::getPostId, postId);
        queryWrapper.eq(Like::getUserId, userId);
        Like existingLike = getOne(queryWrapper);

        if (existingLike != null) {
            // Remove like
            removeById(existingLike.getId());
            
            // Decrement like count
            UpdateWrapper<Post> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", postId);
            updateWrapper.setSql("like_count = like_count - 1");
            postMapper.update(null, updateWrapper);
        }
        
        return Result.ok();
    }

    @Override
    public Result listMyLikes(PostQueryDTO queryDTO) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            userId = 1L; // Test user
        }

        // 1. Get likes by user
        Page<Like> likePage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Like> likeWrapper = new LambdaQueryWrapper<>();
        likeWrapper.eq(Like::getUserId, userId);
        likeWrapper.orderByDesc(Like::getCreateTime);
        Page<Like> resultLikePage = page(likePage, likeWrapper);

        List<Like> likes = resultLikePage.getRecords();
        if (likes.isEmpty()) {
            return Result.ok(new ArrayList<>(), 0L);
        }

        // 2. Get posts
        List<Long> postIds = likes.stream().map(Like::getPostId).collect(Collectors.toList());
        List<Post> posts = postMapper.selectBatchIds(postIds);
        
        // Map posts by ID for order preservation (though we'll reconstruct list)
        java.util.Map<Long, Post> postMap = posts.stream().collect(Collectors.toMap(Post::getId, p -> p));

        List<PostVO> postVOList = likes.stream().map(like -> {
            Post post = postMap.get(like.getPostId());
            if (post == null || post.getIsDeleted() == 1) {
                return null; // Skip deleted posts
            }

            PostVO vo = new PostVO();
            BeanUtils.copyProperties(post, vo);

            // Fetch user info (Author of the post)
            User author = userMapper.selectById(post.getUserId());
            if (author != null) {
                vo.setNickname(author.getNickname());
                vo.setAvatar(author.getAvatar());
            }

            // Fetch category info
            Category category = categoryMapper.selectById(post.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
            return vo;
        })
        .filter(java.util.Objects::nonNull)
        .collect(Collectors.toList());

        return Result.ok(postVOList, resultLikePage.getTotal());
    }
}
