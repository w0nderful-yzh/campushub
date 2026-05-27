package com.yzh.campushub.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PostSearchVO extends PostVO {
    private String highlightedTitle;
    private String highlightedContent;
    private Double score;
}
