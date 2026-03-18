package com.yzh.campushub.controller;

import com.yzh.campushub.dto.CreatePostDTO;
import com.yzh.campushub.dto.PostQueryDTO;
import com.yzh.campushub.dto.Result;
import com.yzh.campushub.dto.UpdatePostDTO;
import com.yzh.campushub.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping
    public Result listPosts(PostQueryDTO queryDTO) {
        return postService.listPosts(queryDTO);
    }

    @GetMapping("/my")
    public Result listMyPosts(PostQueryDTO queryDTO) {
        return postService.listMyPosts(queryDTO);
    }

    @PostMapping
    public Result createPost(@RequestBody CreatePostDTO createPostDTO) {
        postService.createPost(createPostDTO);
        return Result.ok();
    }

    @GetMapping("/{postId}")
    public Result getPostDetail(@PathVariable Long postId) {
        return postService.getPostDetail(postId);
    }

    @PutMapping("/{postId}")
    public Result updatePost(@PathVariable Long postId, @RequestBody UpdatePostDTO updatePostDTO) {
        return postService.updatePost(postId, updatePostDTO);
    }

    @DeleteMapping("/{postId}")
    public Result deletePost(@PathVariable Long postId) {
        return postService.deletePost(postId);
    }
}
