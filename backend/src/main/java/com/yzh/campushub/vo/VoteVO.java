package com.yzh.campushub.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VoteVO {
    private Long id;
    private Long userId;
    private Long postId;
    private String title;
    private String description;
    private Integer maxSelect;
    private Integer isAnonymous;
    private LocalDateTime endTime;
    private Integer totalCount;
    private Integer status;
    private String authorNickname;
    private String authorAvatar;
    private Boolean isVoted;
    private List<VoteOptionVO> options;
    private LocalDateTime createTime;
}
