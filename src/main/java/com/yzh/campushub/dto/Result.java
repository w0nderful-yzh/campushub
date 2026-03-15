package com.yzh.campushub.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yzh.campushub.utils.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result {
    private Integer code; // HTTP状态码
    private String message; // 提示信息
    private Object data; // 数据
    private Long total; // 分页总数

    // 兼容旧字段 success
    public Boolean getSuccess() {
        return code != null && code == 200;
    }

    public static Result ok(){
        return new Result(Constants.CODE_200, Constants.MSG_SUCCESS, null, null);
    }
    public static Result ok(Object data){
        return new Result(Constants.CODE_200, Constants.MSG_SUCCESS, data, null);
    }
    public static Result ok(List<?> data, Long total){
        return new Result(Constants.CODE_200, Constants.MSG_SUCCESS, data, total);
    }
    
    // 默认失败 500
    public static Result fail(String message){
        return new Result(Constants.CODE_500, message, null, null);
    }

    // 指定状态码失败
    public static Result fail(Integer code, String message){
        return new Result(code, message, null, null);
    }
}
