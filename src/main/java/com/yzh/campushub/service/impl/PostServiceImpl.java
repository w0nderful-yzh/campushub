package com.yzh.campushub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzh.campushub.dto.CreatePostDTO;
import com.yzh.campushub.dto.PostQueryDTO;
import com.yzh.campushub.dto.Result;
import com.yzh.campushub.entity.Category;
import com.yzh.campushub.entity.Image;
import com.yzh.campushub.entity.Post;
import com.yzh.campushub.entity.User;
import com.yzh.campushub.mapper.CategoryMapper;
import com.yzh.campushub.mapper.ImageMapper;
import com.yzh.campushub.mapper.PostMapper;
import com.yzh.campushub.mapper.UserMapper;
import com.yzh.campushub.service.PostService;
import com.yzh.campushub.utils.UserContext;
import com.yzh.campushub.vo.PostVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Autowired
    private ImageMapper imageMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public Result listPosts(PostQueryDTO queryDTO) {
        Page<Post> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();

        // category
        if (queryDTO.getCategoryId() != null) {
            wrapper.eq(Post::getCategoryId, queryDTO.getCategoryId());
        }

        // keyword
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            wrapper.and(w -> w.like(Post::getTitle, queryDTO.getKeyword())
                    .or().like(Post::getContent, queryDTO.getKeyword()));
        }

        // status & is_deleted
        wrapper.eq(Post::getStatus, 0); 
        wrapper.eq(Post::getIsDeleted, 0);

        // sort
        if ("hottest".equalsIgnoreCase(queryDTO.getSortType())) {
            wrapper.orderByDesc(Post::getViewCount);
        } else {
            // default latest
            wrapper.orderByDesc(Post::getCreateTime);
        }

        Page<Post> postPage = page(page, wrapper);

        List<PostVO> postVOList = postPage.getRecords().stream().map(post -> {
            PostVO vo = new PostVO();
            BeanUtils.copyProperties(post, vo);

            // fetch user info
            User user = userMapper.selectById(post.getUserId());
            if (user != null) {
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatar());
            }

            // fetch category info
            Category category = categoryMapper.selectById(post.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
            
            return vo;
        }).collect(Collectors.toList());

        return Result.ok(postVOList, postPage.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPost(CreatePostDTO createPostDTO) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            // For testing purposes, default to userId 1 if no user is logged in
            userId = 1L;
        }
        Post post = new Post();
        post.setUserId(userId);
        post.setCategoryId(createPostDTO.getCategoryId());
        post.setTitle(createPostDTO.getTitle());
        post.setContent(createPostDTO.getContent());
        post.setCreateTime(LocalDateTime.now());
        post.setUpdateTime(LocalDateTime.now());
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setFavoriteCount(0);
        post.setStatus(0); // 0: Normal
        post.setIsDeleted(0);
        post.setIsTop(0);

        List<String> images = createPostDTO.getImages();
        if (images != null && !images.isEmpty()) {
            post.setCoverImg(images.get(0));
        }

        save(post);

        if (images != null && !images.isEmpty()) {
            for (int i = 0; i < images.size(); i++) {
                String imgUrl = images.get(i);
                Image image = new Image();
                image.setPostID(post.getId());
                image.setImageUrl(imgUrl);
                image.setSort(i);
                image.setCreateTime(LocalDateTime.now());
                imageMapper.insert(image);
            }
        }
    }
}
