package com.yulaiz.tddl.sequence.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class Result<T> implements Serializable {
    private T data;
    private String code;
    private String message;
    private String msg;

    public void setMessage(String message) {
        this.message = message;
        this.msg = message;
    }

    public void setMsg(String msg) {
        this.message = msg;
        this.msg = msg;
    }

    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode("0");
        result.setMessage("操作成功");
        return result;
    }

    public static <T> Result<T> success(String message) {
        Result<T> result = new Result<>();
        result.setCode("0");
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> success(T t) {
        Result<T> result = new Result<>();
        result.setCode("0");
        result.setMessage("操作成功");
        result.setData(t);
        return result;
    }

    public static <T> Result<T> success(T t, String message) {
        Result<T> result = new Result<>();
        result.setCode("0");
        result.setMessage(message);
        result.setData(t);
        return result;
    }

    public static <T> Result<T> fail() {
        return fail("-1", "系统内部错误");
    }

    public static <T> Result<T> fail(String message) {
        return fail("-1", message);
    }

    public static <T> Result<T> fail(String code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

}
