package com.teak.system.result;

import com.teak.system.result.enums.GlobalResultEnums;
import lombok.Getter;

/**
 * The type Global result.
 *
 * @author 柚mingle木
 * @version 1.0
 * @date 2023 /2/19
 */
@Getter
public class GlobalResult<T> {

    private Integer code;
    private String message;
    private T data = null;


    // 静态工厂方法（线程安全入口）
    public static <T> GlobalResult<T> create() {
        return new GlobalResult<>();
    }

    /**
     * Global result global result.
     * <p>
     * 这里使用的是饿汉式单例，我也不知道为什么怎么写，就是单纯的想写写看，这里的单例确实有效，所有消费者的hashCode都是一样的
     * <p>
     * 2025/2/26 全局返回不能写单例，因为如果使用单例，那么每次调用都会返回同一个对象，多线程返回的数据会被覆盖
     *
     * @return the global result
     */
   /* public static GlobalResult globalResult() {
        return InnClass.globalResult;
    }

    private static class InnClass {
        private static final GlobalResult globalResult = new GlobalResult();
    }*/
    public static GlobalResult<Object> success() {
        return create()
                .setCode(GlobalResultEnums.SUCCESS.getCode())
                .setMessage(GlobalResultEnums.SUCCESS.getMessage());
    }

    @SuppressWarnings("unchecked")
    public static <T> GlobalResult<T> success(T value) {
        return (GlobalResult<T>) create()
                .setCode(GlobalResultEnums.SUCCESS.getCode())
                .setMessage(GlobalResultEnums.SUCCESS.getMessage())
                .setData(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> GlobalResult<T> success(T value, String message) {
        return (GlobalResult<T>) create()
                .setCode(GlobalResultEnums.SUCCESS.getCode())
                .setMessage(message)
                .setData(value);
    }

    public static GlobalResult<Object> error() {
        return create()
                .setCode(GlobalResultEnums.FAIL.getCode())
                .setMessage(GlobalResultEnums.FAIL.getMessage());
    }

    @SuppressWarnings("unchecked")
    public static <T> GlobalResult<T> error(T value) {
        return (GlobalResult<T>) create()
                .setCode(GlobalResultEnums.FAIL.getCode())
                .setMessage(GlobalResultEnums.FAIL.getMessage())
                .setData(value);
    }

    @SuppressWarnings("unchecked")
    public static <T> GlobalResult<T> error(T value, String message) {
        return (GlobalResult<T>) create()
                .setCode(GlobalResultEnums.FAIL.getCode())
                .setMessage(message)
                .setData(value);
    }

    // 重定向响应
    @SuppressWarnings("unchecked")
    public static <T> GlobalResult<T> redirect(T value, String message) {
        return (GlobalResult<T>) create()
                .setCode(GlobalResultEnums.FORWARD.getCode())
                .setMessage(message)
                .setData(value);
    }

    // 优化后的链式方法
    public GlobalResult<T> setCode(Integer code) {
        this.code = code;
        return this;
    }

    public GlobalResult<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    private GlobalResult<T> setData(T data) {
        this.data = data;
        return this;
    }

}
