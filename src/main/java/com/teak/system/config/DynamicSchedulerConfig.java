package com.teak.system.config;

import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledFuture;

/**
 * 动态定时任务配置类
 *
 * <p>执行流程: 数据库加载 → 反射解析参数 → method.invoke() 调用目标方法
 * <p>支持两种存储模式:
 * <ul>
 *   <li>传统模式: params(JSON数组) + parameterTypes(逗号分隔类型名)</li>
 *   <li>推荐模式: taskArgs(JSON键值对) → 保存时自动转换为上述格式</li>
 * </ul>
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

    /** 复用 ObjectMapper，避免每次执行都 new */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /** params 的泛型类型引用 */
    private static final TypeReference<List<Object>> LIST_TYPE_REF = new TypeReference<>() {};

    private volatile boolean running = false;
    private volatile ScheduledFuture<?>[] scheduledFutures = new ScheduledFuture<?>[0];

    // ============ SmartLifecycle 生命周期 ============

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
     * 刷新所有定时任务（取消旧任务 → 从数据库重新加载）
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
                // 启动时预校验：检查 Bean 和方法是否存在（提前暴露问题）
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

    /**
     * 取消所有已调度的任务
     */
    private void cancelTasks() {
        for (ScheduledFuture<?> future : scheduledFutures) {
            if (future != null && !future.isCancelled()) {
                future.cancel(true);
            }
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

    // ============ 任务执行 ============

    /**
     * 创建任务执行 Runnable
     *
     * <p>执行策略:
     * <ul>
     *   <li>有 parameterTypes → 按类型解析 params 并反射调用</li>
     *   <li>有 taskArgs 无 parameterTypes → 从 taskArgs JSON 按位置取值并自动推断类型</li>
     *   <li>都没有 → 无参调用</li>
     * </ul>
     */
    private Runnable createRunnable(SysScheduledTask task) {
        return () -> CompletableFuture.runAsync(() -> executeTask(task), executorService);
    }

    /**
     * 执行单个定时任务的核心方法
     */
    private void executeTask(SysScheduledTask task) {
        String taskId = task.getTaskName();
        long startTime = System.currentTimeMillis();
        log.info("[{}] 开始执行", taskId);

        try {
            Object bean = applicationContext.getBean(task.getBeanName());
            String methodName = task.getMethodName();
            String paramTypesStr = task.getParameterTypes();
            String taskArgsJson = task.getTaskArgs();

            Object result;

            if (hasText(paramTypesStr)) {
                // 传统模式: params + parameterTypes
                result = invokeWithParamTypes(bean, methodName, paramTypesStr, task.getParams(), taskId);
            } else if (hasText(taskArgsJson)) {
                // 推荐模式: taskArgs → 自动推断类型和参数值
                result = invokeWithTaskArgs(bean, methodName, taskArgsJson, taskId);
            } else {
                // 无参模式
                result = invokeNoArgs(bean, methodName, taskId);
            }

            long costMs = System.currentTimeMillis() - startTime;
            log.info("[{}] 执行完成 耗时 {}ms", taskId, costMs);

        } catch (ClassNotFoundException e) {
            log.error("[{}] 参数类型不存在: {}", taskId, e.getMessage());
        } catch (NoSuchMethodException e) {
            log.error("[{}] 目标方法不存在: {}.{}", taskId, task.getBeanName(), task.getMethodName());
        } catch (Exception e) {
            long costMs = System.currentTimeMillis() - startTime;
            log.error("[{}] 执行异常 耗时 {}ms | error={}", taskId, costMs, e.getMessage(), e);
            throw new RuntimeException("任务 [" + taskId + "] 执行失败: " + e.getMessage(), e);
        }
    }

    // ============ 三种调用方式 ============

    /**
     * 方式1: 传统模式 — 通过 parameterTypes + params 反射调用
     */
    private Object invokeWithParamTypes(Object bean, String methodName,
                                        String paramTypesStr, String paramsJson, String taskId)
            throws Exception {
        log.info("[{}] 尝试执行方式1: 传统模式", taskId);
        Class<?>[] classes = resolveParameterClasses(paramTypesStr);
        List<Object> paramValues = parseParams(paramsJson);

        if (paramValues.size() != classes.length) {
            throw new RuntimeException(String.format(
                    "参数个数不匹配: 需要%d个(%s)，实际传入%d个",
                    classes.length, paramTypesStr, paramValues.size()));
        }

        Object[] args = convertParams(paramValues, classes, taskId);
        Method method = bean.getClass().getMethod(methodName, classes);
        return method.invoke(bean, args);
    }

    /**
     * 方式2: 推荐模式 — 通过 taskArgs 自动推断类型后调用
     *
     * <p>从 taskArgs JSON 中按值的位置顺序提取参数，
     * 再通过目标方法的签名自动推断每个位置的类型。
     */
    private Object invokeWithTaskArgs(Object bean, String methodName,
                                      String taskArgsJson, String taskId) throws Exception {
        log.info("[{}] 尝试执行方式2: 推荐模式", taskId);
        Map<String, Object> taskArgs = OBJECT_MAPPER.readValue(taskArgsJson,
                new TypeReference<>() {
                });

        Method targetMethod = findMethod(bean, methodName);
        List<Object> orderedValues = extractOrderedValues(taskArgs);
        Class<?>[] types = targetMethod.getParameterTypes();

        if (orderedValues.size() != types.length) {
            throw new RuntimeException(String.format(
                    "taskArgs参数个数不匹配: 方法需要%d个参数，提供了%d个",
                    types.length, orderedValues.size()));
        }

        Object[] args = convertParams(orderedValues, types, taskId);
        return targetMethod.invoke(bean, args);
    }

    /**
     * 方式3: 无参调用
     */
    private Object invokeNoArgs(Object bean, String methodName, String taskId) throws Exception {
        Method method = bean.getClass().getMethod(methodName);
        return method.invoke(bean);
    }

    // ============ 辅助工具方法 ============

    /**
     * 解析 parameterTypes 字符串为 Class 数组
     */
    private Class<?>[] resolveParameterClasses(String paramTypesStr) throws ClassNotFoundException {
        String[] typeNames = paramTypesStr.split(",");
        Class<?>[] classes = new Class<?>[typeNames.length];
        for (int i = 0; i < typeNames.length; i++) {
            String typeName = typeNames[i].trim();
            // 先尝试基本数据类型
            Class<? extends Serializable> basicType = teakUtils.resolveClassName(typeName);
            if (basicType != null) {
                classes[i] = basicType;
            } else {
                classes[i] = Class.forName(typeName);
            }
        }
        return classes;
    }

    /**
     * 解析 params JSON 为 List
     */
    private List<Object> parseParams(String paramsJson) throws Exception {
        if (paramsJson == null || paramsJson.isBlank()) {
            return List.of();
        }
        return OBJECT_MAPPER.readValue(paramsJson, LIST_TYPE_REF);
    }

    /**
     * 类型转换：将原始参数值逐一转换为目标方法参数类型
     */
    private Object[] convertParams(List<Object> rawValues, Class<?>[] targetTypes, String taskId) {
        Object[] result = new Object[rawValues.size()];
        for (int i = 0; i < rawValues.size(); i++) {
            Object rawValue = rawValues.get(i);
            Class<?> targetType = targetTypes[i];
            result[i] = OBJECT_MAPPER.convertValue(rawValue, targetType);
            log.debug("[{}] 参数[{}] {} -> {}: {}", taskId, i,
                    rawValue.getClass().getSimpleName(), targetType.getSimpleName(), result[i]);
        }
        return result;
    }

    /**
     * 从 taskArgs Map 中按插入顺序提取值（排除 __ 开头的系统字段）
     */
    private List<Object> extractOrderedValues(Map<String, Object> taskArgs) {
        return taskArgs.entrySet().stream()
                .filter(e -> !e.getKey().startsWith("__"))
                .map(Map.Entry::getValue)
                .toList();
    }

    /**
     * 查找目标方法（支持无参/有参/重载场景）
     */
    private Method findMethod(Object bean, String methodName) throws NoSuchMethodException {
        // 优先无参
        try {
            return bean.getClass().getMethod(methodName);
        } catch (NoSuchMethodException ignored) {}

        // 有多个重载时取参数最多的版本
        Method[] candidates = java.util.Arrays.stream(bean.getClass().getMethods())
                .filter(m -> m.getName().equals(methodName))
                .toArray(Method[]::new);
        if (candidates.length == 0) {
            throw new NoSuchMethodException(methodName);
        }
        java.util.Arrays.sort(candidates, java.util.Comparator.comparingInt(m -> -m.getParameterCount()));
        return candidates[0];
    }

    /**
     * 预校验任务：启动时检查 Bean 和方法是否可调用，避免等到执行时才报错
     */
    private void validateTask(SysScheduledTask task) {
        try {
            Object bean = applicationContext.getBean(task.getBeanName());
            String paramTypes = task.getParameterTypes();
            if (hasText(paramTypes)) {
                resolveParameterClasses(paramTypes);
                bean.getClass().getMethod(task.getMethodName(), resolveParameterClasses(paramTypes));
            } else if (hasText(task.getTaskArgs())) {
                findMethod(bean, task.getMethodName());
            } else {
                bean.getClass().getMethod(task.getMethodName());
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                    "任务 [" + task.getTaskName() + "] 预校验失败: " + e.getMessage(), e);
        }
    }

    // ============ Trigger & Event ============

    private Trigger createTrigger(SysScheduledTask task) {
        return triggerContext -> new CronTrigger(task.getCronExpression()).nextExecution(triggerContext);
    }

    @EventListener
    public void handleTaskRefreshEvent(TaskRefreshEvent event) {
        log.info("收到任务刷新事件");
        if (running) {
            refreshTasks();
        }
    }

    private static boolean hasText(String str) {
        return str != null && !str.isBlank();
    }
}
