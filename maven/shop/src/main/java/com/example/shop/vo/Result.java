package com.example.shop.vo;

import lombok.Data;

@Data
public class Result<T> {
    // 响应码（200=成功，500=失败，可自定义）
    private Integer code;
    // 响应提示语
    private String msg;
    // 响应数据（泛型，支持任意类型：单个对象、列表、null）
    private T data;

    /**
     * 成功响应（带数据）
     * 
     * @param data 数据
     * @return result实体
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 成功响应（无数据）
     * 
     * @param msg 响应提示
     * @return result实体
     */
    public static <T> Result<T> success(String msg) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }

    /**
     * 失败响应
     * 
     * @param msg 响应提示
     * @return result实体
     */
    public static <T> Result<T> fail(String msg) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }
}