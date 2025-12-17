package com.teak.system.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teak.mapper.SysScheduledTaskMapper;
import com.teak.model.SysScheduledTask;
import com.teak.system.event.TaskRefreshEvent;
import com.teak.system.utils.TeakUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * Created with: IntelliJ IDEA
 *
 * @Author: li zheng
 * @Date: 2025/5/4 00:01
 * @Project: teakWeb
 * @File: DynamicSchedulerConfig.java
 * @Description: 动态定时任务配置类，只能支持基本数据类型，引用类型，不支持泛型数据
 */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class DynamicSchedulerConfig implements SmartLifecycle {

    private final SysScheduledTaskMapper sysScheduledTaskMapper;

    private final ApplicationContext applicationContext;

    private final ThreadPoolTaskScheduler scheduler;

    private final ExecutorService executorService;

    private final TeakUtils teakUtils;

    private boolean running = false;

    // 存储当前所有已调度的任务
    private ScheduledFuture<?>[] scheduledFutures = new ScheduledFuture[0];

    @Override
    public void start() {
        if (!running) {
            refreshTasks();
            running = true;
            log.info("动态定时任务配置已启动");
        }
    }

    @Override
    public void stop() {
        cancelTasks();
        running = false;
        log.info("动态定时任务配置已停止");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * 刷新所有定时任务
     */
    public void refreshTasks() {
        log.info("开始刷新定时任务");
        // 取消所有当前任务
        cancelTasks();

        // 重新加载任务
        List<SysScheduledTask> tasks = sysScheduledTaskMapper.findByStatus(1);
        log.info("加载 {} 个定时任务", tasks.size());

        // 创建新的任务数组
        scheduledFutures = new ScheduledFuture[tasks.size()];

        // 注册所有任务
        for (int i = 0; i < tasks.size(); i++) {
            SysScheduledTask task = tasks.get(i);
            scheduledFutures[i] = scheduler.schedule(
                    createRunnable(task),
                    createTrigger(task)
            );
        }

        log.info("定时任务刷新完成");
    }

    /**
     * 取消所有已调度的任务
     */
    private void cancelTasks() {
        for (ScheduledFuture<?> future : scheduledFutures) {
            if (future != null && !future.isCancelled()) {
                future.cancel(true);
            }
        }
        scheduledFutures = new ScheduledFuture[0];
        log.info("已取消所有定时任务");
    }

    private Runnable createRunnable(SysScheduledTask task) {
        return () -> CompletableFuture.runAsync(() -> {
            log.info("当前线程是否为守护线程：" + Thread.currentThread().isDaemon());
            try {
                log.info("执行任务[{}]", task.getTaskName());
                Object bean = applicationContext.getBean(task.getBeanName());

                // 获取参数类型
                String parameterTypes = task.getParameterTypes();
                if (parameterTypes != null) {
                    String[] typeNames = parameterTypes.split(",");
                    Class<?>[] classes = new Class[typeNames.length];
                    for (int i = 0; i < typeNames.length; i++) {
                        //先匹配基本数据类型，如果匹配不到，再匹配引用数据类型
                        //至于为什么不将基本数据类型和引用数据类型分开写，是因为基本数据类型是无法使用Class.forName进行反射得到结果
                        Class<? extends Serializable> aClass = teakUtils.resolveClassName(typeNames[i]);
                        if (aClass != null) {
                            classes[i] = aClass;
                        } else {
                            classes[i] = Class.forName(typeNames[i].trim());
                        }
                    }

                    //解析参数
                    List list = new ObjectMapper().readValue(task.getParams(), List.class);
                    if (list.size() == classes.length) {

                        Object[] array = new Object[list.size()];

                        for (int i = 0; i < list.size(); i++) {
                            Object rawValue = list.get(i);
                            Class<?> targetType = classes[i];
                            log.info("正在转换参数 {}: 原始类型={}, 目标类型={}", i,
                                    rawValue.getClass().getSimpleName(),
                                    targetType.getSimpleName());

                            array[i] = new ObjectMapper().convertValue(rawValue, targetType);

                            log.info("转换结果 {}: [{}] {}", i,
                                    array[i].getClass().getSimpleName(),
                                    array[i]);
                        }

                        Method method = bean.getClass().getMethod(task.getMethodName(), classes);
                        method.invoke(bean, array);
                        log.info("==================================================================================================================");
                    } else {
                        throw new RuntimeException("参数个数不匹配");
                    }

                } else {
                    //这里是进行无参调用
                    Method method = bean.getClass().getMethod(task.getMethodName());
                    method.invoke(bean);
                }

            } catch (Exception e) {
                log.error("执行任务[{}]异常: {}", task.getTaskName(), e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    private Trigger createTrigger(SysScheduledTask task) {
        return triggerContext -> {
            CronTrigger cronTrigger = new CronTrigger(task.getCronExpression());
            return cronTrigger.nextExecution(triggerContext);
        };
    }

    /**
     * 监听任务刷新事件
     */
    @EventListener
    public void handleTaskRefreshEvent(TaskRefreshEvent event) {
        log.info("收到任务刷新事件");
        if (running) {
            refreshTasks();
        }
        log.info("定时任务刷新事件处理完成");
    }
}