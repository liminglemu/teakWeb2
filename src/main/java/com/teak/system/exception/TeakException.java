package com.teak.system.exception;

/**
 * 系统基础异常（所有业务异常的父类）
 *
 * <p>继承 RuntimeException（无需声明抛出，简化使用），
 * 但提供明确的异常分类，便于 GlobalExceptionHandler 统一处理。
 */
public class TeakException extends RuntimeException {

    public TeakException(String message) {
        super(message);
    }

    public TeakException(String message, Throwable cause) {
        super(message, cause);
    }
}
