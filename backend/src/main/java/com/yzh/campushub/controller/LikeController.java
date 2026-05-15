package com.yzh.campushub.controller;


import com.yzh.campushub.dto.PostQueryDTO;
import com.yzh.campushub.dto.Result;
import com.yzh.campushub.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/post-likes")
public class LikeController {

    @Autowired
    private LikeService likeService;

    @PostMapping("/{postId}")
    public Result likePost(@PathVariable Long postId) {
        return likeService.likePost(postId);
    }

    @DeleteMapping("/{postId}")
    public Result unlikePost(@PathVariable Long postId) {
        return likeService.unlikePost(postId);
    }

    @GetMapping("/my")
    public Result listMyLikes(PostQueryDTO queryDTO) {
        return likeService.listMyLikes(queryDTO);
    }
}
