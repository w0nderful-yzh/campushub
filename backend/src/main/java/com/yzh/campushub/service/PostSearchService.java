package com.yzh.campushub.service;

import com.yzh.campushub.dto.PostSearchQueryDTO;
import com.yzh.campushub.dto.Result;
import com.yzh.campushub.entity.Post;

public interface PostSearchService {
    Result search(PostSearchQueryDTO queryDTO);

    Result suggest(String keyword, Integer size);

    Result reindexAll();

    void indexPost(Post post);

    void deletePost(Long postId);
}
