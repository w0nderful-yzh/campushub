package com.yzh.campushub.dto;

import lombok.Data;

@Data
public class PostSearchQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private Long categoryId;
    private String sortType = "relevance"; // relevance, latest
    private String keyword;
}
