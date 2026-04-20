package com.teak.system.config;

import com.teak.mapper.SysScheduledTaskMapper;
import com.teak.model.SysScheduledTask;
import com.teak.system.event.TaskRefreshEvent;
import com.teak.system.executor.TaskExecutor;
import com.teak.system.exception.TaskExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * 动态定时任务调度器 — 纯调度职责
 *
 * <p>负责: 任务的注册、取消、刷新、Trigger/Runnable 创建
 * <p>不负责: 参数解析、反射调用、日志记录（全部委托给 {@link TaskExecutor}）
 */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class DynamicSchedulerConfig implements SmartLifecycle {

    private final SysScheduledTaskMapper sysScheduledTaskMapper;
    private final TaskExecutor taskExecutor;
    private final ApplicationContext applicationContext;
    private final ThreadPoolTaskScheduler scheduler;
    private final ExecutorService executorService;

    private volatile boolean running = false;
    private volatile ScheduledFuture<?>[] scheduledFutures = new ScheduledFuture<?>[0];

    /** 缓存每个任务最近一次 CronTrigger 计划的触发时间 (Instant→LocalDateTime) */
    private final ConcurrentHashMap<String, LocalDateTime> scheduledFireTimeMap = new ConcurrentHashMap<>();

    // ============ SmartLifecycle ============

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

    // ============ 任务管理 ============

    /**
     * 刷新所有定时任务
     */
    public void refreshTasks() {
        log.info("开始刷新定时任务");
        cancelTasks();

        List<SysScheduledTask> tasks = sysScheduledTaskMapper.findByStatus(1);
        if (tasks.isEmpty()) {
            log.info("无活跃定时任务");
            return;
        }

        log.info("加载 {} 个定时任务", tasks.size());
        scheduledFutures = new ScheduledFuture<?>[tasks.size()];
        for (int i = 0; i < tasks.size(); i++) {
            SysScheduledTask task = tasks.get(i);
            try {
                validateTask(task);
                scheduledFutures[i] = scheduler.schedule(createRunnable(task), createTrigger(task));
                log.info("[{}] 已注册: {}.{} | cron={}", task.getTaskName(),
                        task.getBeanName(), task.getMethodName(), task.getCronExpression());
            } catch (Exception e) {
                log.error("[{}] 注册失败: {}", task.getTaskName(), e.getMessage());
            }
        }
        log.info("定时任务刷新完成 (成功 {}/{})", countActiveFutures(), tasks.size());
    }

    private void cancelTasks() {
        for (ScheduledFuture<?> f : scheduledFutures) {
            if (f != null && !f.isCancelled()) f.cancel(true);
        }
        scheduledFutures = new ScheduledFuture<?>[0];
    }

    private int countActiveFutures() {
        int count = 0;
        for (ScheduledFuture<?> f : scheduledFutures) {
            if (f != null && !f.isCancelled()) count++;
        }
        return count;
    }

    // ============ Trigger & Runnable（调度层核心） ============

    /**
     * 创建 Runnable — 正常调度时从 Map 取出 scheduledFireTime，然后委托给 TaskExecutor
     */
    private Runnable createRunnable(SysScheduledTask task) {
        String taskName = task.getTaskName();
        return () -> CompletableFuture.runAsync(() -> {
            LocalDateTime fireTime = scheduledFireTimeMap.remove(taskName);
            if (fireTime == null) {
                log.warn("[{}] 未获取到 scheduledFireTime，回退使用当前时间", taskName);
                fireTime = LocalDateTime.now();
            }
            taskExecutor.execute(task, TaskExecutor.SOURCE_SCHEDULED, fireTime);
        }, executorService);
    }

    /**
     * 创建 Trigger — 计算下次执行时间并缓存到 Map
     */
    private Trigger createTrigger(SysScheduledTask task) {
        return triggerContext -> {
            Instant nextInstant = new CronTrigger(task.getCronExpression())
                    .nextExecution(triggerContext);

            if (nextInstant != null) {
                LocalDateTime fireTime = LocalDateTime.ofInstant(nextInstant, ZoneId.systemDefault());
                scheduledFireTimeMap.put(task.getTaskName(), fireTime);
                log.debug("[{}] Trigger → nextFireTime={}", task.getTaskName(), fireTime);
            }
            return nextInstant;
        };
    }

    /**
     * 预校验：启动时检查 Bean + 方法是否存在
     */
    private void validateTask(SysScheduledTask task) throws NoSuchMethodException, ClassNotFoundException {
        Object bean = applicationContext.getBean(task.getBeanName());
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
        String methodName = task.getMethodName();
        String paramTypes = task.getParameterTypes();

        if (isNotBlank(paramTypes)) {
            // 模式1: 传统 parameterTypes — 精确匹配参数类型
            targetClass.getMethod(methodName,
                    Arrays.stream(paramTypes.split(","))
                            .map(String::trim)
                            .map(t -> {
                                try { return t.contains(".") ? Class.forName(t) : Class.forName("java.lang." + t); }
                                catch (ClassNotFoundException e) { throw new TaskExecutionException("任务预校验时参数类型不存在: " + e.getMessage(), e); }
                            })
                            .toArray(Class[]::new));
        } else {
            // 模式2 / 模式3: taskArgs 或无参 — 按方法名搜索（支持有参方法）
            Method foundMethod = findAnyMethodByName(targetClass, methodName);
            log.debug("[{}] 预校验通过: {}({})", task.getTaskName(), foundMethod.getName(),
                    java.util.Arrays.toString(foundMethod.getParameterTypes()));
        }
    }

    /**
     * 按名称查找目标类中的任意重载方法（与 TaskExecutor.findMethod 逻辑一致）
     */
    private static Method findAnyMethodByName(Class<?> targetClass, String methodName) throws NoSuchMethodException {
        // 先尝试精确匹配无参版本
        try {
            return targetClass.getMethod(methodName);
        } catch (NoSuchMethodException ignored) { }

        // 再搜索所有 public 方法（含重载）
        Method[] candidates = Arrays.stream(targetClass.getMethods())
                .filter(m -> m.getName().equals(methodName))
                .toArray(Method[]::new);

        if (candidates.length == 0) {
            throw new NoSuchMethodException(targetClass.getSimpleName() + "." + methodName);
        }

        // 多个重载时优先返回参数最多的版本
        Arrays.sort(candidates, java.util.Comparator.comparingInt(m -> -m.getParameterCount()));
        return candidates[0];
    }

    private static boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }

    // ============ Event ============

    @EventListener
    public void handleTaskRefreshEvent(TaskRefreshEvent event) {
        log.info("收到任务刷新事件");
        if (running) refreshTasks();
    }
}
