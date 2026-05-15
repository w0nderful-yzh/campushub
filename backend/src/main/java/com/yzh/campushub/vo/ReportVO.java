package com.yzh.campushub.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReportVO {
    private Long id;
    private Long reportUserId;
    private String reportNickname;
    private Integer targetType;
    private Long targetId;
    private String reason;
    private Integer status;
    private Long handleUserId;
    private String handleNickname;
    private String handleResult;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
