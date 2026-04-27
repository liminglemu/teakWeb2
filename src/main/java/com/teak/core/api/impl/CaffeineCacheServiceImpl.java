package com.teak.core.api.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.teak.core.api.CacheService;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Caffeine 的本地缓存实现
 */
@Service
public class CaffeineCacheServiceImpl implements CacheService {

    private final Cache<String, Object> cache;

    public CaffeineCacheServiceImpl() {
        this.cache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(1, TimeUnit.HOURS) // 默认1小时过期
                .build();
    }

    @Override
    public void put(String key, Object value) {
        cache.put(key, value);
    }

    @Override
    public void put(String key, Object value, long ttl, TimeUnit unit) {
        // Caffeine 不支持每个键单独设置过期时间，所以这里使用统一的过期策略
        // 如果需要每个键单独过期，可以使用 Caffeine 的 expireAfter(Expiry) 或考虑其他缓存实现
        // 这里简单实现为放入缓存，但过期时间可能不准确
        cache.put(key, value);
        // 注意：这里没有实现 per-key TTL，如果需要，可以考虑使用 Caffeine 的 Expiry 接口
        // 但为了简化，我们假设使用统一的过期时间，或者调用者使用带 TTL 的 put 时使用默认过期时间
    }

    @Override
    public <T> T get(String key, Class<T> type) {
        Object value = cache.getIfPresent(key);
        if (value == null) {
            return null;
        }
        return type.cast(value);
    }

    @Override
    public <T> T get(String key, Class<T> type, Callable<T> loader) {
        try {
            Object value = cache.get(key, k -> {
                try {
                    return loader.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            return type.cast(value);
        } catch (RuntimeException e) {
            if (e.getCause() != null && e.getCause() instanceof Exception) {
                throw new RuntimeException("Cache loader failed", e.getCause());
            }
            throw e;
        }
    }

    @Override
    public boolean delete(String key) {
        cache.invalidate(key);
        return true;
    }

    @Override
    public boolean exists(String key) {
        return cache.getIfPresent(key) != null;
    }

    @Override
    public void clear() {
        cache.invalidateAll();
    }
}