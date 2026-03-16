package com.yzh.campushub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yzh.campushub.entity.Post;
import com.yzh.campushub.dto.CreatePostDTO;
import com.yzh.campushub.dto.PostQueryDTO;
import com.yzh.campushub.dto.Result;

public interface PostService extends IService<Post> {
    void createPost(CreatePostDTO createPostDTO);
    
    Result listPosts(PostQueryDTO queryDTO);
}
