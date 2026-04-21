package com.yzh.campushub.dto;

import lombok.Data;

@Data
public class PostQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private Long categoryId;
    private String sortType; // latest, hottest
    private String keyword;
}
