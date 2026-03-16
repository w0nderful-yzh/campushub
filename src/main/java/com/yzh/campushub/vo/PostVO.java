package com.yzh.campushub.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostVO {
    private Long id;
    private Long userId;
    private String nickname;
    private String avatar;
    private Long categoryId;
    private String categoryName;
    private String title;
    private String content;
    private String coverImg;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer favoriteCount;
    private Integer status;
    private Integer isTop;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
