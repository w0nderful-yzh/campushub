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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Autowired
    private ImageMapper imageMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private com.yzh.campushub.mapper.LikeMapper likeMapper;

    @Autowired
    private com.yzh.campushub.mapper.FavoriteMapper favoriteMapper;

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

    @Override
    public Result getPostDetail(Long postId) {
        Post post = getById(postId);
        if (post == null || post.getIsDeleted() == 1) {
            return Result.fail("Post not found");
        }

        // Increment view count
        post.setViewCount(post.getViewCount() + 1);
        updateById(post);

        com.yzh.campushub.vo.PostDetailVO vo = new com.yzh.campushub.vo.PostDetailVO();
        BeanUtils.copyProperties(post, vo);

        // Author info
        User user = userMapper.selectById(post.getUserId());
        if (user != null) {
            com.yzh.campushub.vo.PostDetailVO.Author author = new com.yzh.campushub.vo.PostDetailVO.Author();
            author.setId(user.getId());
            author.setNickname(user.getNickname());
            author.setAvatar(user.getAvatar());
            vo.setAuthor(author);
        }

        // Images
        LambdaQueryWrapper<Image> imageWrapper = new LambdaQueryWrapper<>();
        imageWrapper.eq(Image::getPostID, postId);
        imageWrapper.orderByAsc(Image::getSort);
        List<Image> imageList = imageMapper.selectList(imageWrapper);
        if (imageList != null) {
            List<String> images = imageList.stream().map(Image::getImageUrl).collect(Collectors.toList());
            vo.setImages(images);
        }

        // Interaction status
        Long currentUserId = UserContext.getUserId();
        if (currentUserId != null) {
            Long likeCount = likeMapper.selectCount(new LambdaQueryWrapper<com.yzh.campushub.entity.Like>()
                    .eq(com.yzh.campushub.entity.Like::getPostId, postId)
                    .eq(com.yzh.campushub.entity.Like::getUserId, currentUserId));
            vo.setIsLiked(likeCount > 0);

            Long favCount = favoriteMapper.selectCount(new LambdaQueryWrapper<com.yzh.campushub.entity.Favorite>()
                    .eq(com.yzh.campushub.entity.Favorite::getPostId, postId)
                    .eq(com.yzh.campushub.entity.Favorite::getUserId, currentUserId));
            vo.setIsFavorited(favCount > 0);
        } else {
            vo.setIsLiked(false);
            vo.setIsFavorited(false);
        }

        return Result.ok(vo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updatePost(Long postId, com.yzh.campushub.dto.UpdatePostDTO updatePostDTO) {
        log.info("开始更新帖子，ID: {}, 内容: {}", postId, updatePostDTO);

        // 1. 获取当前用户
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            // 测试环境默认用户
            currentUserId = 1L;
        }

        // 2. 查询帖子是否存在
        Post post = getById(postId);
        if (post == null || post.getIsDeleted() == 1) {
            log.warn("更新失败，帖子不存在或已删除，ID: {}", postId);
            return Result.fail("帖子不存在");
        }

        // 3. 校验权限（只能修改自己的帖子）
        if (!post.getUserId().equals(currentUserId)) {
            log.warn("更新失败，无权修改，帖子ID: {}, 当前用户ID: {}, 作者ID: {}", postId, currentUserId, post.getUserId());
            return Result.fail("无权修改此帖子");
        }

        // 4. 更新帖子基本信息
        post.setCategoryId(updatePostDTO.getCategoryId());
        post.setTitle(updatePostDTO.getTitle());
        post.setContent(updatePostDTO.getContent());
        post.setUpdateTime(LocalDateTime.now());

        // 更新封面图（取第一张图片）
        List<String> images = updatePostDTO.getImages();
        if (images != null && !images.isEmpty()) {
            post.setCoverImg(images.get(0));
        } else {
            post.setCoverImg("");
        }

        updateById(post);

        // 5. 更新图片关联信息
        // 先删除旧图片
        LambdaQueryWrapper<Image> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(Image::getPostID, postId);
        imageMapper.delete(deleteWrapper);

        // 再插入新图片
        if (images != null && !images.isEmpty()) {
            for (int i = 0; i < images.size(); i++) {
                String imgUrl = images.get(i);
                Image image = new Image();
                image.setPostID(postId);
                image.setImageUrl(imgUrl);
                image.setSort(i);
                image.setCreateTime(LocalDateTime.now());
                imageMapper.insert(image);
            }
        }
        
        log.info("帖子更新成功，ID: {}", postId);
        return Result.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deletePost(Long postId) {
        log.info("开始删除帖子，ID: {}", postId);

        // 1. 获取当前用户
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            // 测试环境默认用户
            currentUserId = 1L;
        }

        // 2. 查询帖子
        Post post = getById(postId);
        if (post == null || post.getIsDeleted() == 1) {
            log.warn("删除失败，帖子不存在或已删除，ID: {}", postId);
            return Result.fail("帖子不存在");
        }

        // 3. 校验权限
        if (!post.getUserId().equals(currentUserId)) {
            log.warn("删除失败，无权操作，帖子ID: {}, 当前用户ID: {}, 作者ID: {}", postId, currentUserId, post.getUserId());
            return Result.fail("无权删除此帖子");
        }

        // 4. 逻辑删除
        post.setIsDeleted(1);
        post.setUpdateTime(LocalDateTime.now());
        updateById(post);

        log.info("帖子删除成功，ID: {}", postId);
        return Result.ok();
    }

    @Override
    public Result listMyPosts(PostQueryDTO queryDTO) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            // Test environment default user
            userId = 1L;
        }
        
        Page<Post> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();

        // filter by current user
        wrapper.eq(Post::getUserId, userId);
        // filter out deleted posts
        wrapper.eq(Post::getIsDeleted, 0);
        
        // standard ordering
        wrapper.orderByDesc(Post::getCreateTime);

        Page<Post> postPage = page(page, wrapper);

        List<PostVO> postVOList = postPage.getRecords().stream().map(post -> {
            PostVO vo = new PostVO();
            BeanUtils.copyProperties(post, vo);

            // fetch user info (though it's the current user)
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
}
