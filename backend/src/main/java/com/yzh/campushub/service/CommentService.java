package com.yzh.campushub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yzh.campushub.dto.CreateCommentDTO;
import com.yzh.campushub.dto.Result;
import com.yzh.campushub.entity.Comment;

import java.util.List;

public interface CommentService extends IService<Comment> {
    Result createComment(CreateCommentDTO createCommentDTO);

    /**
     * Get comment list for a post
     * @param postId
     * @return
     */
    Result listPostComments(Long postId);

    /**
     * Delete comment
     * @param commentId
     * @return
     */
    Result deleteComment(Long commentId);
}
