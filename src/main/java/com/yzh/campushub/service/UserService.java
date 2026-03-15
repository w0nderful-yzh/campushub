package com.yzh.campushub.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yzh.campushub.dto.UpdateProfileDTO;
import com.yzh.campushub.entity.User;
import com.yzh.campushub.vo.UserHomeVO;
import org.springframework.web.multipart.MultipartFile;

public interface UserService extends IService<User> {
    UserHomeVO getUserHomeInfo(Long userId);
    
    void updateProfile(Long userId, UpdateProfileDTO updateProfileDTO);
    
    String updateAvatar(Long userId, MultipartFile file);
}
