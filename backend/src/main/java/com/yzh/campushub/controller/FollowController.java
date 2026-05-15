package com.yzh.campushub.controller;

import com.yzh.campushub.dto.Result;
import com.yzh.campushub.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/follows")
public class FollowController {

    @Autowired
    private FollowService followService;

    @PostMapping("/{userId}")
    public Result toggleFollow(@PathVariable Long userId) {
        return followService.toggleFollow(userId);
    }

    @GetMapping("/following")
    public Result listFollowing(@RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize) {
        return followService.listFollowing(pageNum, pageSize);
    }

    @GetMapping("/followers")
    public Result listFollowers(@RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize) {
        return followService.listFollowers(pageNum, pageSize);
    }

    @GetMapping("/{userId}/count")
    public Result getFollowCount(@PathVariable Long userId) {
        return followService.getFollowCount(userId);
    }
}
