package com.uxsino.common.response;

/**
 * @className ResultCode
 * @description 响应码
 * @date 2021/1/13 15:49
 */
public enum ResultCode {
                        SUCCESS(200,"操作成功"),
                        SUCCESS_ING(201,"操作成功，已经正在处理该请求"),
                        SUCCESS_SEARCH(202,"查询实例创建进度成功，修改配置失败"),
                        SUCCESS_REPEATE(205,"上传文件重复,请重新选择上传文件!"),
                        FAILED(500,"操作失败"),
                        VALIDATE_FAILED(400,"参数检验失败"),
                        UNAUTHORIZED(401,"暂未登录或token已经过期"),
                        FORBIDDEN(403,"没有相关权限");

    private int code;

    private String message;

    ResultCode() {
    }

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
