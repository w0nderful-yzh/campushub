package com.yzh.campushub.dto;

import lombok.Data;

@Data
public class CreateReportDTO {
    private Integer targetType;
    private Long targetId;
    private String reason;
}
