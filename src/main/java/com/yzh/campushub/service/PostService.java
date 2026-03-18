package com.yzh.campushub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yzh.campushub.entity.Post;
import com.yzh.campushub.dto.CreatePostDTO;
import com.yzh.campushub.dto.PostQueryDTO;
import com.yzh.campushub.dto.Result;

public interface PostService extends IService<Post> {
    void createPost(CreatePostDTO createPostDTO);
    
    Result listPosts(PostQueryDTO queryDTO);

    Result getPostDetail(Long postId);

    /**
     * 修改帖子
     */
    Result updatePost(Long postId, com.yzh.campushub.dto.UpdatePostDTO updatePostDTO);

    /**
     * 删除帖子
     * @param postId
     * @return
     */
    Result deletePost(Long postId);

    /**
     * 查询我发布的帖子
     * @param queryDTO
     * @return
     */
    Result listMyPosts(com.yzh.campushub.dto.PostQueryDTO queryDTO);
}
