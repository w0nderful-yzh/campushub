package com.yzh.campushub.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NoticeVO {
    private Long id;
    private Integer type;
    private String content;
    private Long senderUserId;
    private String senderNickname;
    private String senderAvatar;
    private Long postId;
    private Long commentId;
    private Integer isRead;
    private LocalDateTime createTime;
}
