package com.yzh.campushub.dto;

import lombok.Data;

@Data
public class SendMessageDTO {
    private Long receiverId;
    private String content;
}
