package com.yzh.campushub.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdatePostDTO {
    private Long categoryId;
    private String title;
    private String content;
    private List<String> images;
}
