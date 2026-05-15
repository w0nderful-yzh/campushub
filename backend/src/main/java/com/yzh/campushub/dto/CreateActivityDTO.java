package com.yzh.campushub.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateActivityDTO {
    private String title;
    private String description;
    private String coverImg;
    private String location;
    private Integer activityType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer maxParticipants;
}
