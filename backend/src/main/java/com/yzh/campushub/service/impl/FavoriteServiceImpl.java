package com.yzh.campushub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzh.campushub.dto.PostQueryDTO;
import com.yzh.campushub.dto.Result;
import com.yzh.campushub.entity.Category;
import com.yzh.campushub.entity.Favorite;
import com.yzh.campushub.entity.Post;
import com.yzh.campushub.entity.User;
import com.yzh.campushub.mapper.CategoryMapper;
import com.yzh.campushub.mapper.FavoriteMapper;
import com.yzh.campushub.mapper.PostMapper;
import com.yzh.campushub.mapper.UserMapper;
import com.yzh.campushub.service.FavoriteService;
import com.yzh.campushub.utils.UserContext;
import com.yzh.campushub.vo.PostVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result favoritePost(Long postId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            userId = 1L; // Test user
        }

        // Check if post exists
        Post post = postMapper.selectById(postId);
        if (post == null || post.getIsDeleted() == 1) {
            return Result.fail("Post not found");
        }

        // Check if already favorited
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getPostId, postId);
        queryWrapper.eq(Favorite::getUserId, userId);
        Favorite existingFav = getOne(queryWrapper);

        UpdateWrapper<Post> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", postId);

        if (existingFav != null) {
            // Already favorited, so unfavorite
            removeById(existingFav.getId());
            // Decrement favorite count
            updateWrapper.setSql("favorite_count = favorite_count - 1");
        } else {
            // Not favorited, so favorite
            Favorite newFav = new Favorite();
            newFav.setPostId(postId);
            newFav.setUserId(userId);
            newFav.setCreateTime(LocalDateTime.now());
            save(newFav);
            // Increment favorite count
            updateWrapper.setSql("favorite_count = favorite_count + 1");
        }

        postMapper.update(null, updateWrapper);

        return Result.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result unfavoritePost(Long postId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            userId = 1L; // Test user
        }

        // Check if post exists
        Post post = postMapper.selectById(postId);
        if (post == null || post.getIsDeleted() == 1) {
            return Result.fail("Post not found");
        }

        // Check if already favorited
        LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Favorite::getPostId, postId);
        queryWrapper.eq(Favorite::getUserId, userId);
        Favorite existingFav = getOne(queryWrapper);

        if (existingFav != null) {
            // Already favorited, so unfavorite
            removeById(existingFav.getId());
            // Decrement favorite count
            UpdateWrapper<Post> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", postId);
            updateWrapper.setSql("favorite_count = favorite_count - 1");
            postMapper.update(null, updateWrapper);
        }

        return Result.ok();
    }

    @Override
    public Result listMyFavorites(PostQueryDTO queryDTO) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            userId = 1L; // Test user
        }

        // 1. Get favorites by user
        Page<Favorite> favoritePage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Favorite> favoriteWrapper = new LambdaQueryWrapper<>();
        favoriteWrapper.eq(Favorite::getUserId, userId);
        favoriteWrapper.orderByDesc(Favorite::getCreateTime);
        Page<Favorite> resultFavoritePage = page(favoritePage, favoriteWrapper);

        List<Favorite> favorites = resultFavoritePage.getRecords();
        if (favorites.isEmpty()) {
            return Result.ok(new ArrayList<>(), 0L);
        }

        // 2. Get posts
        List<Long> postIds = favorites.stream().map(Favorite::getPostId).collect(Collectors.toList());
        List<Post> posts = postMapper.selectBatchIds(postIds);
        
        // Map posts by ID for order preservation (though we'll reconstruct list)
        java.util.Map<Long, Post> postMap = posts.stream().collect(Collectors.toMap(Post::getId, p -> p));

        List<PostVO> postVOList = favorites.stream().map(fav -> {
            Post post = postMap.get(fav.getPostId());
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

        return Result.ok(postVOList, resultFavoritePage.getTotal());
    }
}
