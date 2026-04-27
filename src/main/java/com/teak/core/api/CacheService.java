package com.teak.core.api;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务接口，用于抽象不同的缓存后端（Redis、Caffeine、内存等）。
 */
public interface CacheService {

    /**
     * 存入缓存
     */
    void put(String key, Object value);

    /**
     * 存入缓存并设置过期时间
     */
    void put(String key, Object value, long ttl, TimeUnit unit);

    /**
     * 获取缓存值
     */
    <T> T get(String key, Class<T> type);

    /**
     * 获取缓存值，如果不存在则通过loader加载
     */
    <T> T get(String key, Class<T> type, Callable<T> loader);

    /**
     * 删除缓存
     */
    boolean delete(String key);

    /**
     * 检查缓存是否存在
     */
    boolean exists(String key);

    /**
     * 清空所有缓存
     */
    void clear();
}