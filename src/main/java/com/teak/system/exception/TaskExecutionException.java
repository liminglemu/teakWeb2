package com.teak.system.exception;

/**
 * 定时任务执行异常 — 用于 TaskExecutor 层
 *
 * <p>场景：反射调用失败、参数不匹配、任务执行超时等
 */
public class TaskExecutionException extends TeakException {

    public TaskExecutionException(String message) {
        super(message);
    }

    public TaskExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
