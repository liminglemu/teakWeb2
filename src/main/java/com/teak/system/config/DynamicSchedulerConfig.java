package com.teak.system.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teak.business.service.SysScheduledTaskService;
import com.teak.model.SysScheduledTask;
import com.teak.system.utils.TeakUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

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
public class DynamicSchedulerConfig implements SchedulingConfigurer {

    private final SysScheduledTaskService sysScheduledTaskService;

    private final ApplicationContext applicationContext;

    private final ThreadPoolTaskScheduler scheduler;

    private final ExecutorService executorService;

    private final TeakUtils teakUtils;

    @Override
    public void configureTasks(ScheduledTaskRegistrar registrar) {
        registrar.setTaskScheduler(scheduler);
        List<SysScheduledTask> tasks = sysScheduledTaskService.findByStatus(1);
        tasks.forEach(task -> registrar.addTriggerTask(
                createRunnable(task),
                createTrigger(task)
        ));
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
                log.error("执行任务[{}]异常: {}", task.getTaskName(), e.getMessage());
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
}
