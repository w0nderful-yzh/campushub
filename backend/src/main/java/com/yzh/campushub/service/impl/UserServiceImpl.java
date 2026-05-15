package com.yzh.campushub.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzh.campushub.dto.UpdateProfileDTO;
import com.yzh.campushub.entity.User;
import com.yzh.campushub.mapper.UserMapper;
import com.yzh.campushub.service.UserService;
import com.yzh.campushub.vo.UserHomeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    @Override
    public UserHomeVO getUserHomeInfo(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        UserHomeVO userHomeVO = new UserHomeVO();
        BeanUtil.copyProperties(user, userHomeVO);
        return userHomeVO;
    }

    @Override
    public void updateProfile(Long userId, UpdateProfileDTO updateProfileDTO) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        
        user.setNickname(updateProfileDTO.getNickname());
        user.setGender(updateProfileDTO.getGender());
        user.setEmail(updateProfileDTO.getEmail());
        user.setCollege(updateProfileDTO.getCollege());
        user.setMajor(updateProfileDTO.getMajor());
        user.setProfile(updateProfileDTO.getProfile());
        user.setUpdateTime(LocalDateTime.now());
        
        updateById(user);
        log.info("用户个人资料更新成功: userId={}", userId);
    }

    @Override
    public String updateAvatar(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File cannot be empty");
        }
        
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        
        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                throw new RuntimeException("Invalid file");
            }
            
            // 生成文件名: 时间戳_UUID_原始文件名
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFileName = timestamp + "_" + UUID.randomUUID() + fileExtension;
            
            // 创建上传目录
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            // 保存文件
            File destFile = new File(uploadDir, newFileName);
            file.transferTo(destFile);
            
            // 更新用户头像路径
            String avatarPath = "uploads/" + newFileName;
            user.setAvatar(avatarPath);
            user.setUpdateTime(LocalDateTime.now());
            updateById(user);
            
            log.info("用户头像更新成功: userId={}, avatar={}", userId, avatarPath);
            return avatarPath;
        } catch (IOException e) {
            log.error("头像上传失败: userId={}", userId, e);
            throw new RuntimeException("Failed to upload avatar: " + e.getMessage());
        }
    }
}
