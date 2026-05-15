package com.yzh.campushub.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ActivitySignupVO {
    private Long userId;
    private String nickname;
    private String avatar;
    private LocalDateTime signupTime;
}
