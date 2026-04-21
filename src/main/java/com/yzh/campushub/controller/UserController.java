package com.yzh.campushub.controller;


import com.yzh.campushub.dto.Result;
import com.yzh.campushub.dto.UpdateProfileDTO;
import com.yzh.campushub.service.UserService;
import com.yzh.campushub.vo.UserHomeVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<Result> getUserHomeInfo(@PathVariable Long userId) {
        log.info("查询用户主页信息: userId={}", userId);
        UserHomeVO userHomeVO = userService.getUserHomeInfo(userId);
        return ResponseEntity.ok(Result.ok(userHomeVO));
    }

    /**
     * 更新用户个人资料
     * @param userId             用户ID
     * @param updateProfileDTO   包含更新资料的对象
     * @return Result
     */
    @PutMapping("/profile")
    public ResponseEntity<Result> updateProfile(@RequestParam Long userId, @RequestBody UpdateProfileDTO updateProfileDTO) {
        log.info("更新用户个人资料: userId={}", userId);
        userService.updateProfile(userId, updateProfileDTO);
        return ResponseEntity.ok(Result.ok("个人资料更新成功"));
    }

    /**
     * 更新用户头像
     * @param userId 用户ID
     * @param file   头像文件
     * @return Result 包含新的头像访问路径
     */
    @PostMapping("/avatar")
    public ResponseEntity<Result> updateAvatar(@RequestParam Long userId, @RequestParam MultipartFile file) {
        log.info("更新用户头像: userId={}", userId);
        String avatarPath = userService.updateAvatar(userId, file);
        return ResponseEntity.ok(Result.ok(avatarPath));
    }
}
