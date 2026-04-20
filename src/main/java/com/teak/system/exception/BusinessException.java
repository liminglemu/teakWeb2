package com.teak.system.exception;

/**
 * 业务校验异常 — 用于 Service/Controller 层的业务规则校验
 *
 * <p>场景：参数非法、数据不存在、状态冲突等可预知的业务错误
 */
public class BusinessException extends TeakException {

    private final String code;

    public BusinessException(String message) {
        super(message);
        this.code = "BIZ_ERROR";
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = "BIZ_ERROR";
    }

    public String getCode() {
        return code;
    }
}
