package com.yzh.campushub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yzh.campushub.dto.Result;
import com.yzh.campushub.entity.Favorite;

import com.yzh.campushub.dto.PostQueryDTO;

public interface FavoriteService extends IService<Favorite> {
    /**
     * Favorite post
     * @param postId
     * @return
     */
    Result favoritePost(Long postId);

    /**
     * Unfavorite post
     * @param postId
     * @return
     */
    Result unfavoritePost(Long postId);

    /**
     * List my favorites
     * @param queryDTO
     * @return
     */
    Result listMyFavorites(PostQueryDTO queryDTO);
}
