package com.yzh.campushub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yzh.campushub.dto.Result;
import com.yzh.campushub.entity.Like;

public interface LikeService extends IService<Like> {
    /**
     * Toggle like post
     * @param postId
     * @return
     */
    Result likePost(Long postId);

    /**
     * Cancel like (unlike)
     * @param postId
     * @return
     */
    Result unlikePost(Long postId);

    /**
     * List my likes
     * @param queryDTO
     * @return
     */
    Result listMyLikes(com.yzh.campushub.dto.PostQueryDTO queryDTO);
}
