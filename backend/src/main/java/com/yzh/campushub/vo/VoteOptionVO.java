package com.yzh.campushub.vo;

import lombok.Data;

@Data
public class VoteOptionVO {
    private Long id;
    private String content;
    private Integer count;
    private Double percentage;
    private Boolean isSelected;
}
