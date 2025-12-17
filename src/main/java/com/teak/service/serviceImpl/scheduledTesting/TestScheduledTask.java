package com.teak.service.serviceImpl.scheduledTesting;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 测试定时任务的示例类
 */
@Component
@Slf4j
public class TestScheduledTask {
    
    /**
     * 无参数的定时任务方法
     */
    public void executeSimpleTask() {
        log.info("执行简单定时任务，当前时间: {}", LocalDateTime.now());
    }
    
    /**
     * 带参数的定时任务方法
     * @param message 消息内容
     * @param count 执行次数
     */
    public void executeTaskWithParameters(String message, Integer count) {
        log.info("执行带参数的定时任务: message={}, count={}, 当前时间: {}", message, count, LocalDateTime.now());
    }
}