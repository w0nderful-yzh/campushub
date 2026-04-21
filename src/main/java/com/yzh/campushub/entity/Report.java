package com.yzh.campushub.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("report")
public class Report implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    private Long reportUserId;
    private Integer targetType;
    private Long targetId;
    private String reason;
    private Integer status;
    private Long handleUserId;
    private String handleResult;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
