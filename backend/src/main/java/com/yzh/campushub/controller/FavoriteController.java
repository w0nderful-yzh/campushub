package com.yzh.campushub.controller;


import com.yzh.campushub.dto.PostQueryDTO;
import com.yzh.campushub.dto.Result;
import com.yzh.campushub.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/post-favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @PostMapping("/{postId}")
    public Result favoritePost(@PathVariable Long postId) {
        return favoriteService.favoritePost(postId);
    }

    @DeleteMapping("/{postId}")
    public Result unfavoritePost(@PathVariable Long postId) {
        return favoriteService.unfavoritePost(postId);
    }

    @GetMapping("/my")
    public Result listMyFavorites(PostQueryDTO queryDTO) {
        return favoriteService.listMyFavorites(queryDTO);
    }
}
