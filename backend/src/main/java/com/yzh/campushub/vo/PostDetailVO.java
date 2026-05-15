package com.yzh.campushub.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostDetailVO {
    private Long id;
    private String title;
    private String content;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer favoriteCount;
    private LocalDateTime createTime;
    private Author author;
    private List<String> images;
    private Boolean isLiked;
    private Boolean isFavorited;

    @Data
    public static class Author {
        private Long id;
        private String nickname;
        private String avatar;
    }
}
