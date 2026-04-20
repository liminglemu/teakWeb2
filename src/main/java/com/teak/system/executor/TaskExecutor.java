package com.teak.system.executor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.teak.mapper.SysScheduledTaskLogMapper;
import com.teak.model.SysScheduledTask;
import com.teak.model.SysScheduledTaskLog;
import com.teak.system.utils.TeakUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 定时任务执行器 — 负责单次任务的完整执行生命周期
 *
 * <p>职责: 日志记录 → 反射参数解析 → method.invoke → 结果记录
 * <p>与 DynamicSchedulerConfig 解耦: 调度器只管"何时触发"，执行器管"如何执行"
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskExecutor {

    private final ApplicationContext applicationContext;
    private final SysScheduledTaskLogMapper taskLogMapper;
    private final TeakUtils teakUtils;

    /** 触发来源常量 */
    public static final String SOURCE_SCHEDULED = "SCHEDULED";
    public static final String SOURCE_MANUAL = "MANUAL";
    public static final String SOURCE_BACKFILL = "BACKFILL";

    /**
     * 执行单个定时任务（统一入口）
     *
     * <p>完整生命周期:
     * <ol>
     *   <li>插入执行记录 (status=0 运行中)</li>
     *   <li>根据任务配置选择解析策略并反射调用</li>
     *   <li>更新执行记录 (1成功/2失败 + costMs)</li>
     * </ol>
     *
     * @param task          任务配置
     * @param triggerSource 触发来源 {@link #SOURCE_SCHEDULED} / {@link #SOURCE_MANUAL} / {@link #SOURCE_BACKFILL}
     * @param fireTime      触发执行时间（正常调度=计划时间, 区间补执行=历史时间点）
     */
    public void execute(SysScheduledTask task, String triggerSource, LocalDateTime fireTime) {
        String taskName = task.getTaskName();
        long startTime = System.currentTimeMillis();

        // 设置 ThreadLocal 上下文（业务代码可随时获取 fireTime）
        TaskExecuteContext.set(fireTime, taskName);

        // 1. 写入执行记录
        SysScheduledTaskLog logEntity = createAndSaveLog(task, triggerSource, fireTime);
        log.info("[{}][logId={}] 开始执行 | source={} | fireTime={}",
                taskName, logEntity.getId(), triggerSource, fireTime);

        try {
            // 2. 反射调用业务方法（三种模式合一）
            doInvoke(task, taskName);

            // 3. 成功
            finishLog(logEntity, startTime, true, null);
            log.info("[{}][logId={}] 执行完成 | cost={}ms", taskName, logEntity.getId(),
                    System.currentTimeMillis() - startTime);

        } catch (ClassNotFoundException e) {
            finishLog(logEntity, startTime, false, "参数类型不存在: " + e.getMessage());
            log.error("[{}][logId={}] 参数类型不存在: {}", taskName, logEntity.getId(), e.getMessage());
        } catch (NoSuchMethodException e) {
            finishLog(logEntity, startTime, false,
                    "目标方法不存在: " + task.getBeanName() + "." + task.getMethodName());
            log.error("[{}][logId={}] 目标方法不存在: {}.{}", taskName, logEntity.getId(),
                    task.getBeanName(), task.getMethodName());
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            finishLog(logEntity, startTime, false, errorMsg);
            log.error("[{}][logId={}] 执行失败 | cost={}ms | error={}", taskName, logEntity.getId(),
                    System.currentTimeMillis() - startTime, errorMsg, e);
            throw new RuntimeException("任务 [" + taskName + "] 执行失败: " + errorMsg, e);
        } finally {
            // 清理 ThreadLocal，防止内存泄漏
            TaskExecuteContext.clear();
        }
    }

    // ==================== 内部实现 ====================

    /**
     * 创建并保存执行记录（status=0 运行中）
     */
    private SysScheduledTaskLog createAndSaveLog(SysScheduledTask task, String source, LocalDateTime fireTime) {
        SysScheduledTaskLog logEntity = new SysScheduledTaskLog();
        logEntity.setTaskId(task.getId());
        logEntity.setTaskName(task.getTaskName());
        logEntity.setFireTime(fireTime);
        logEntity.setTriggerSource(source);
        logEntity.setStatus(0); // 运行中
        taskLogMapper.insert(logEntity);
        return logEntity;
    }

    /**
     * 更新执行结果
     */
    private void finishLog(SysScheduledTaskLog logEntity, long startMs, boolean success, String errorMsg) {
        logEntity.setStatus(success ? 1 : 2);
        logEntity.setCostMs(System.currentTimeMillis() - startMs);
        if (!success && CharSequenceUtil.isNotBlank(errorMsg)) {
            logEntity.setErrorMessage(CharSequenceUtil.length(errorMsg) > 2000
                    ? CharSequenceUtil.sub(errorMsg, 0, 2000) : errorMsg);
        }
        taskLogMapper.updateById(logEntity);
    }

    /**
     * 反射调用目标方法 — 三种模式自动路由（一个方法内完成）
     */
    private void doInvoke(SysScheduledTask task, String taskName) throws Exception {
        Object bean = applicationContext.getBean(task.getBeanName());
        String methodName = task.getMethodName();
        String paramTypesStr = task.getParameterTypes();
        String taskArgsJson = task.getTaskArgs();

        if (CharSequenceUtil.isNotBlank(paramTypesStr)) {
            // 模式1: 传统 parameterTypes + params
            invokeByParamTypes(bean, methodName, paramTypesStr, task.getParams(), taskName);
        } else if (CharSequenceUtil.isNotBlank(taskArgsJson)) {
            // 模式2: 推荐 taskArgs（JSON键值对，自动类型推断）
            invokeByTaskArgs(bean, methodName, taskArgsJson, taskName);
        } else {
            // 模式3: 无参
            Method method = bean.getClass().getMethod(methodName);
            method.invoke(bean);
        }
    }

    /**
     * 模式1: 通过 parameterTypes 字符串解析参数类型 + params JSON 取值
     */
    private void invokeByParamTypes(Object bean, String methodName,
                                    String paramTypesStr, String paramsJson,
                                    String taskName) throws Exception {
        log.info("[{}] 模式1: 传统 parameterTypes", taskName);
        Class<?>[] classes = resolveParameterClasses(paramTypesStr);

        List<Object> paramValues = StrUtil.isBlank(paramsJson)
                ? CollUtil.newArrayList()
                : JSONUtil.toList(paramsJson, Object.class);

        if (paramValues.size() != classes.length) {
            throw new RuntimeException(String.format(
                    "参数个数不匹配: 需要%d个(%s)，实际传入%d个",
                    classes.length, paramTypesStr, paramValues.size()));
        }

        Object[] args = convertParams(paramValues, classes, taskName);
        Method method = bean.getClass().getMethod(methodName, classes);
        method.invoke(bean, args);
    }

    /**
     * 模式2: 从 taskArgs JSON 按位置取值 + 自动推断目标类型
     */
    private void invokeByTaskArgs(Object bean, String methodName,
                                  String taskArgsJson, String taskName) throws Exception {
        log.info("[{}] 模式2: taskArgs 自动推断", taskName);
        Map<String, Object> taskArgs = JSONUtil.toBean(taskArgsJson, Map.class);
        Method targetMethod = findMethod(bean, methodName);

        // 按 Map 插入顺序提取值，排除 __ 开头的系统字段
        List<Object> orderedValues = CollUtil.newArrayList();
        for (Map.Entry<String, Object> entry : taskArgs.entrySet()) {
            if (!entry.getKey().startsWith("__")) {
                orderedValues.add(entry.getValue());
            }
        }

        Class<?>[] types = targetMethod.getParameterTypes();

        if (orderedValues.size() != types.length) {
            throw new RuntimeException(String.format(
                    "taskArgs参数个数不匹配: 方法需要%d个参数，提供了%d个",
                    types.length, orderedValues.size()));
        }

        Object[] args = convertParams(orderedValues, types, taskName);
        targetMethod.invoke(bean, args);
    }

    /**
     * 将 parameterTypes 逗号字符串解析为 Class 数组
     */
    private Class<?>[] resolveParameterClasses(String paramTypesStr) throws ClassNotFoundException {
        String[] typeNames = paramTypesStr.split(",");
        Class<?>[] classes = new Class[typeNames.length];
        for (int i = 0; i < typeNames.length; i++) {
            String typeName = typeNames[i].trim();
            Class<? extends Serializable> basicType = teakUtils.resolveClassName(typeName);
            classes[i] = basicType != null ? basicType : Class.forName(typeName);
        }
        return classes;
    }

    /**
     * 类型转换：原始值列表 → 目标参数类型数组
     */
    private Object[] convertParams(List<Object> rawValues, Class<?>[] targetTypes, String taskId) {
        Object[] result = new Object[rawValues.size()];
        for (int i = 0; i < rawValues.size(); i++) {
            result[i] = BeanUtil.toBean(rawValues.get(i), targetTypes[i]);
            log.debug("[{}] 参数[{}] {} -> {}: {}", taskId, i,
                    rawValues.get(i).getClass().getSimpleName(),
                    targetTypes[i].getSimpleName(), result[i]);
        }
        return result;
    }

    /**
     * 查找目标方法（无参优先 → 有参选最多参数的版本）
     */
    private Method findMethod(Object bean, String methodName) throws NoSuchMethodException {
        try {
            return bean.getClass().getMethod(methodName);
        } catch (NoSuchMethodException ignored) {

            Method[] candidates = java.util.Arrays.stream(bean.getClass().getMethods())
                    .filter(m -> m.getName().equals(methodName))
                    .toArray(Method[]::new);
            if (candidates.length == 0) {
                throw new NoSuchMethodException(methodName);
            }
            java.util.Arrays.sort(candidates, java.util.Comparator.comparingInt(m -> -m.getParameterCount()));
            return candidates[0];
        }
    }
}
