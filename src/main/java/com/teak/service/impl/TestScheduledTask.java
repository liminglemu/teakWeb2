package com.teak.service.impl;

import com.teak.rabbitmq.config.RabbitMQConfig;
import com.teak.system.executor.TaskExecuteContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;


/**
 * 测试定时任务的示例类
 */
@Component
@Slf4j
public class TestScheduledTask {
    private final ExecutorService executorService;
    private final RabbitTemplate rabbitTemplate;

    public TestScheduledTask(ExecutorService executorService, RabbitTemplate rabbitTemplate) {
        this.executorService = executorService;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 无参数的定时任务方法
     */
    public void executeSimpleTask() {
        String taskName = TaskExecuteContext.getTaskName();
        LocalDateTime fireTime = TaskExecuteContext.getFireTime();
        log.info("任务名称: {},触发时间: {},执行简单定时任务，当前时间: {}", taskName, fireTime, LocalDateTime.now());
        for (int i = 0; i < 10; i++) {
            CompletableFuture.runAsync(() -> {
                rabbitTemplate.convertAndSend(RabbitMQConfig.HELLO_QUEUE, taskName + "," + fireTime);
            }, executorService);
        }
    }

    /**
     * 带参数的定时任务方法
     *
     * @param message 消息内容
     * @param count   执行次数
     */
    public void executeTaskWithParameters(String message, Integer count) {
        log.info("执行带参数的定时任务: message={}, count={}, 执行时间: {},当前时间: {}", message, count, TaskExecuteContext.getFireTime(), LocalDateTime.now());
    }
}