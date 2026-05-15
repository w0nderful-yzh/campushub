package com.yzh.campushub.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageVO {
    private Long id;
    private Long senderId;
    private String senderNickname;
    private String senderAvatar;
    private String content;
    private Integer isRead;
    private LocalDateTime createTime;
}
