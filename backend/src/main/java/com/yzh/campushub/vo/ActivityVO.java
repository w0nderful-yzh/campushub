package com.yzh.campushub.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ActivityVO {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private String coverImg;
    private String location;
    private Integer activityType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer maxParticipants;
    private Integer currentCount;
    private Integer status;
    private String authorNickname;
    private String authorAvatar;
    private Boolean isSignedUp;
    private LocalDateTime createTime;
}
