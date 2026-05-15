package com.yzh.campushub.controller;

import com.yzh.campushub.dto.CreateCommentDTO;
import com.yzh.campushub.dto.Result;
import com.yzh.campushub.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping
    public Result createComment(@RequestBody CreateCommentDTO createCommentDTO) {
        return commentService.createComment(createCommentDTO);
    }

    @GetMapping("/post/{postId}")
    public Result listPostComments(@PathVariable Long postId) {
        return commentService.listPostComments(postId);
    }

    @DeleteMapping("/{commentId}")
    public Result deleteComment(@PathVariable Long commentId) {
        return commentService.deleteComment(commentId);
    }
}
