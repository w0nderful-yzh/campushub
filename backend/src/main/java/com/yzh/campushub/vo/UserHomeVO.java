package com.yzh.campushub.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserHomeVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private Integer gender;
    private String college;
    private String major;
    private String profile;
    private LocalDateTime createTime;
}
