package com.teak.system.config.handler;

import com.teak.system.exception.BusinessException;
import com.teak.system.exception.TaskExecutionException;
import com.teak.system.result.GlobalResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局异常处理器 — 按异常类型分层处理，返回统一格式的响应
 *
 * <p>处理优先级（从具体到泛型）:
 * <ol>
 *   <li>{@link BusinessException} → 400 + 错误消息</li>
 *   <li>{@link TaskExecutionException} → 500 + 任务执行错误</li>
 *   <li>{@link IllegalArgumentException} → 400 + 参数错误</li>
 *   <li>{@link MethodArgumentNotValidException} → 400 + 校验失败</li>
 *   <li>{@link Exception} → 500 + 兜底</li>
 * </ol>
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.teak.controller")
public class GlobalExceptionHandler {

    /**
     * 业务校验异常（参数非法、数据不存在、状态冲突等）
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public GlobalResult<?> handleBusinessException(BusinessException e) {
        log.warn("业务校验异常: {}", e.getMessage());
        return GlobalResult.error(e.getMessage());
    }

    /**
     * 定时任务执行异常（反射调用失败、参数不匹配等）
     */
    @ExceptionHandler(TaskExecutionException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public GlobalResult<?> handleTaskExecutionException(TaskExecutionException e) {
        log.error("定时任务执行异常: {}", e.getMessage(), e);
        return GlobalResult.error("任务执行失败: " + e.getMessage());
    }

    /**
     * 参数不合法异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public GlobalResult<?> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("参数不合法: {}", e.getMessage());
        return GlobalResult.error(e.getMessage());
    }

    /**
     * 参数校验失败（@Valid 注解触发）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public GlobalResult<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        log.warn("参数校验失败: {}", msg);
        return GlobalResult.error(msg);
    }

    /**
     * 绑定异常（类型转换等）
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public GlobalResult<?> handleBindException(BindException e) {
        String msg = e.getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数绑定失败");
        log.warn("参数绑定失败: {}", msg);
        return GlobalResult.error(msg);
    }

    /**
     * 兜底：未分类的未知异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public GlobalResult<?> handleException(Exception e) {
        log.error("系统内部异常", e);
        return GlobalResult.error("服务器内部错误");
    }

    private ConcurrentHashMap<String, Object> concurrentHashMapPut(Exception e) {
        ConcurrentHashMap<String, Object> concurrentHashMap = new ConcurrentHashMap<>(5);
        concurrentHashMap.put("本地化消息", e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "N/A");
        concurrentHashMap.put("原因", e.getCause() != null ? e.getCause() : "N/A");
        concurrentHashMap.put("类", e.getClass());
        concurrentHashMap.put("已抑制", e.getSuppressed() != null ? e.getSuppressed() : "N/A");
        concurrentHashMap.put("哈希代码", e.hashCode());
        log.error(String.valueOf(concurrentHashMap));
        return concurrentHashMap;
    }


    private GlobalResult getResult(ConcurrentHashMap<String, Object> concurrentHashMap) {
        return GlobalResult.error(concurrentHashMap);
    }
}
