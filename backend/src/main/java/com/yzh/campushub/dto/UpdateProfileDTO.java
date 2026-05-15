package com.yzh.campushub.dto;

import lombok.Data;

@Data
public class UpdateProfileDTO {
    private String nickname;
    private Integer gender;
    private String email;
    private String college;
    private String major;
    private String profile;
}
