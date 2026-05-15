package com.yzh.campushub.vo;

import lombok.Data;

@Data
public class FollowUserVO {
    private Long id;
    private String nickname;
    private String avatar;
    private String college;
    private Boolean isFollowed;
}
