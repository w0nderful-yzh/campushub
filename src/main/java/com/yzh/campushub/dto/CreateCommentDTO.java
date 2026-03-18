package com.yzh.campushub.dto;

import lombok.Data;

@Data
public class CreateCommentDTO {
    private Long postId;
    private Long parentId;
    private Long replyUserId;
    private String content;
}
