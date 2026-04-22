package com.uxsino.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @className ResultInfo
 * @description 结果集的封装
 * @date 2021/1/13 15:42
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultInfo<T> {
    // 状态码
    private int resultCode;

    // 消息
    private String message;

    // 数据
    private T data;

    //
    private String errorLog;

    /**
     * 成功返回结果
     *
     * @param data 获取的数据
     */
    public static <T> ResultInfo<T> success(T data) {
        return new ResultInfo<T>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data, null);
    }

    /**
     * 成功返回结果
     *
     * @param data    获取的数据
     * @param message 提示信息
     */
    public static <T> ResultInfo<T> success(String message, T data) {
        return new ResultInfo<T>(ResultCode.SUCCESS.getCode(), message, data, null);
    }

    /**
     * 成功返回结果
     *
     * @param message 获取的数据
     */
    public static <T> ResultInfo<T> success(String message) {
        return new ResultInfo<T>(ResultCode.SUCCESS.getCode(), message, null, null);
    }

    /**
     * 操作成功，已经正在处理该请求
     *
     * @param data 获取的数据
     */
    public static <T> ResultInfo<T> success_ing(T data) {
        return new ResultInfo<T>(ResultCode.SUCCESS_ING.getCode(), ResultCode.SUCCESS.getMessage(), data, null);
    }

    /**
     * 参数校验失败
     *
     * @param message
     * @param <T>
     * @return
     */
    public static <T> ResultInfo<T> validateFailed(String message) {
        return new ResultInfo<T>(ResultCode.VALIDATE_FAILED.getCode(), message, null, null);
    }

    /**
     * 没有权限
     *
     * @param message
     * @param <T>
     * @return
     */
    public static <T> ResultInfo<T> forbidden(String message) {
        return new ResultInfo<T>(ResultCode.FORBIDDEN.getCode(), message, null, null);
    }

    /**
     * 失败返回结果
     *
     * @param <T>
     * @return
     */
    public static <T> ResultInfo<T> failed() {
        return new ResultInfo<T>(ResultCode.FAILED.getCode(), ResultCode.FAILED.getMessage(), null, null);
    }

    /**
     * 失败返回结果
     *
     * @param message 提示信息
     */
    public static <T> ResultInfo<T> failed(String message) {
        return new ResultInfo<T>(ResultCode.FAILED.getCode(), message, null, null);
    }

    /**
     * 失败返回结果
     *
     * @param errorCode 错误码
     * @param message   错误信息
     */
    public static <T> ResultInfo<T> failed(ResultCode errorCode, String message) {
        return new ResultInfo<T>(errorCode.getCode(), message, null, null);
    }

    /**
     * 未验证
     *
     * @param message
     * @param <T>
     * @return
     */
    public static <T> ResultInfo<T> unAuthorized(String message) {
        return new ResultInfo<T>(ResultCode.UNAUTHORIZED.getCode(), message, null, null);
    }

}
