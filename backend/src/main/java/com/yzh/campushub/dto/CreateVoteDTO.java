package com.yzh.campushub.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateVoteDTO {
    private Long postId;
    private String title;
    private String description;
    private Integer maxSelect;
    private Integer isAnonymous;
    private LocalDateTime endTime;
    private List<String> options;
}
