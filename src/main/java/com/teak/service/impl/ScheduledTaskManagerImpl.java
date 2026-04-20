package com.teak.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.teak.mapper.SysScheduledTaskMapper;
import com.teak.model.SysScheduledTask;
import com.teak.model.vo.SysScheduledTaskVo;
import com.teak.service.ScheduledTaskManager;
import com.teak.system.executor.TaskExecutor;
import com.teak.system.event.TaskRefreshEvent;
import com.teak.system.event.TaskRefreshEvent;
import com.teak.system.exception.BusinessException;
import com.teak.system.utils.TeakUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * 定时任务管理器实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledTaskManagerImpl implements ScheduledTaskManager {

    private final SysScheduledTaskMapper sysScheduledTaskMapper;
    private final TeakUtils teakUtils;
    private final ApplicationContext applicationContext;
    private final TaskExecutor taskExecutor;
    private final ExecutorService executorService;

    @Override
    public List<SysScheduledTask> getAllTasks() {
        return sysScheduledTaskMapper.getAllTask();
    }

    /**
     * 添加并启动定时任务（增强版：自动解析 + 预校验 + taskArgs智能参数）
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addAndStartScheduledTask(SysScheduledTaskVo vo) {
        // 1. 解析 methodPath（快速模式）
        ResolveMethodPathResult resolveMethodPathResult = resolveMethodPath(vo);

        // 2. 标准化字段
        String beanName = teakUtils.lowerFirstCharAndTrim(resolveMethodPathResult.beanName());
        String methodName = teakUtils.lowerFirstCharAndTrim(resolveMethodPathResult.methodName());

        // 3. 校验 Bean 是否存在
        validateBeanExists(beanName);

        // 4. 根据参数模式进行校验和转换
        boolean useTaskArgs = vo.getTaskArgs() != null && !vo.getTaskArgs().isEmpty();
        if (useTaskArgs) {
            // 新模式: taskArgs — 自动推断参数类型，无需 parameterTypes
            resolveTaskArgs(beanName, methodName, vo);
        } else {
            // 传统模式: params + parameterTypes 兼容旧接口
            validateMethodExists(beanName, methodName, vo.getParameterTypes(), vo.getParams());
        }

        // 5. 去重校验
        checkDuplicateTaskName(vo.getTaskName());

        // 6. 构建实体并保存
        SysScheduledTask scheduledTask = new SysScheduledTask();
        scheduledTask.setTaskName(vo.getTaskName());
        scheduledTask.setCronExpression(vo.getCronExpression());
        scheduledTask.setBeanName(beanName);
        scheduledTask.setMethodName(methodName);
        scheduledTask.setStatus(vo.getStatus() != null ? vo.getStatus() : 1);

        if (useTaskArgs) {
            // 推荐模式: 只存 taskArgs，执行层 invokeWithTaskArgs() 自动推断类型和参数值
            scheduledTask.setTaskArgs(JSONUtil.toJsonStr(vo.getTaskArgs()));
        } else {
            // 传统模式存储
            scheduledTask.setParameterTypes(teakUtils.resolveReferenceClassName(vo.getParameterTypes()));
            if (CollUtil.isNotEmpty(vo.getParams())) {
                scheduledTask.setParams(JSONUtil.toJsonStr(vo.getParams()));
            }
        }

        sysScheduledTaskMapper.insert(scheduledTask);
        log.info("成功添加定时任务: {} [{}.{}]", vo.getTaskName(), beanName, methodName);

        // 刷新定时任务
        refreshScheduledTasks();
    }

    /**
     * 智能解析 taskArgs — 按位置顺序自动匹配参数并推断类型
     *
     * <p>用户传入的 JSON 值按顺序依次对应方法的第1、第2...个参数，
     * 键名仅作标识用途（不影响匹配），系统自动从方法签名推断目标类型。
     *
     * <p>示例：方法 getDeviceFaultRecords(String startTime, String endTime)
     * <pre>
     * taskArgs: {"startTime":"2022-04-01","endTime":"2024-08-01"}
     *   → 参数0="2022-04-01"(String), 参数1="2024-08-01"(String)
     *
     * taskArgs: {"a":"2022-04-01","b":"2024-08-01"}   ← 键名随意
     *   → 参数0="2022-04-01"(String), 参数1="2024-08-01"(String)
     * </pre>
     */
    private void resolveTaskArgs(String beanName, String methodName, SysScheduledTaskVo vo) {
        try {
            Object bean = applicationContext.getBean(beanName);
            Method targetMethod = findTargetMethod(bean, methodName);

            Parameter[] parameters = targetMethod.getParameters();
            Map<String, Object> taskArgs = vo.getTaskArgs();

            if (parameters.length == 0) {
                if (!taskArgs.isEmpty()) {
                    log.warn("方法 {}.{} 无参数，但提供了 taskArgs，将忽略参数", beanName, methodName);
                }
                vo.getTaskArgs().put("__resolvedTypes", new String[0]);
                return;
            }

            // 按位置顺序取值（JSON的values顺序与插入顺序一致）
            List<Object> orderedParams = new ArrayList<>();
            List<String> resolvedTypeNames = new ArrayList<>();
            List<Object> values = new ArrayList<>(taskArgs.values());

            for (int i = 0; i < parameters.length; i++) {
                Class<?> paramType = parameters[i].getType();
                Object rawValue = values.get(i);

                // 自动类型转换（Hutool: BeanUtil.toBean 支持类型转换）
                Object convertedValue = BeanUtil.toBean(rawValue, paramType);
                orderedParams.add(convertedValue);
                resolvedTypeNames.add(paramType.getName());

                log.info("参数[{}] = {} ({}) → {}", i, rawValue,
                        rawValue.getClass().getSimpleName(), paramType.getSimpleName());
            }

            // 存储推断的类型供执行层使用
            vo.getTaskArgs().put("__resolvedTypes", resolvedTypeNames.toArray(new String[0]));

            log.info("taskArgs 解析完成: {}.{} 参数={}, 类型={}",
                    beanName, methodName, orderedParams, resolvedTypeNames);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("taskArgs 解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查找目标方法（支持方法重载场景）
     * 同时搜索接口和实现类上的 @Param 注解
     */
    private Method findTargetMethod(Object bean, String methodName) throws NoSuchMethodException {
        // 优先查找无参方法
        try {
            return bean.getClass().getMethod(methodName);
        } catch (NoSuchMethodException ignored) {
        }

        // 列出所有同名方法
        Method[] candidates = ArrayUtil.filter(bean.getClass().getMethods(),
                m -> m.getName().equals(methodName));

        if (candidates.length == 0) {
            throw new NoSuchMethodException("方法不存在: " + methodName);
        }

        if (candidates.length == 1) {
            return candidates[0];
        }

        // 多个重载方法时，返回参数最多的那个（通常是最完整的版本）
        Arrays.sort(candidates, Comparator.comparingInt(m -> -m.getParameterCount()));
        log.warn("发现{}个重载的 {} 方法，选择参数最多的版本", candidates.length, methodName);
        return candidates[0];
    }


    /**
     * 解析 methodPath 为 beanName 和 methodName
     * 支持格式: "beanName.methodName"
     */
    private ResolveMethodPathResult resolveMethodPath(SysScheduledTaskVo vo) {
        if (vo.getMethodPath() == null || vo.getMethodPath().isBlank()) {
            return new ResolveMethodPathResult(null, null);
        }
        String path = vo.getMethodPath().trim();
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex <= 0 || dotIndex >= path.length() - 1) {
            throw new IllegalArgumentException("methodPath 格式错误，应为: beanName.methodName，示例: deviceFaultRecordFetchTask.fetchDeviceFaultRecords");
        }
        String beanName = path.substring(0, dotIndex);
        String methodName = path.substring(dotIndex + 1);
        log.info("解析 methodPath: {} -> beanName={}, methodName={}", path, beanName, methodName);
        return new ResolveMethodPathResult(beanName, methodName);
    }

    private record ResolveMethodPathResult(String beanName, String methodName) {
    }

    /**
     * 校验 Bean 是否存在
     */
    private void validateBeanExists(String beanName) {
        if (!applicationContext.containsBeanDefinition(beanName)) {
            // 尝试按接口类型查找相似Bean
            String[] candidateBeans = applicationContext.getBeanNamesForType(Object.class);
            List<String> similar = CollUtil.newArrayList();
            for (String name : candidateBeans) {
                if (name.toLowerCase().contains(beanName.toLowerCase()) ||
                        beanName.toLowerCase().contains(name.toLowerCase())) {
                    similar.add(name);
                }
            }
            String hint = CollUtil.isEmpty(similar)
                    ? "提示: Bean名称为类名首字母小写(如 DeviceFaultRecordFetchTask -> deviceFaultRecordFetchTask)"
                    : "相似的Bean名称: " + CharSequenceUtil.join(", ", similar);
            throw new IllegalArgumentException("Bean不存在: [" + beanName + "]。 " + hint);
        }
    }

    /**
     * 校验方法是否存在且参数匹配
     */
    private void validateMethodExists(String beanName, String methodName, String parameterTypes, List<?> params) {
        try {
            Object bean = applicationContext.getBean(beanName);
            Class<?>[] paramClasses = null;
        if (parameterTypes != null && !parameterTypes.isBlank()) {
            String[] typeNames = parameterTypes.split(",");
            paramClasses = new Class[typeNames.length];
            for (int i = 0; i < typeNames.length; i++) {
                Class<? extends Serializable> aClass = teakUtils.resolveClassName(typeNames[i].trim());
                paramClasses[i] = aClass != null ? aClass : Class.forName(typeNames[i].trim());
            }
        } else if (CollUtil.isNotEmpty(params)) {
            // 自动推断参数类型
            paramClasses = new Class[params.size()];
            for (int i = 0; i < params.size(); i++) {
                paramClasses[i] = params.get(i).getClass();
            }
            }

            Method method;
            if (paramClasses != null && paramClasses.length > 0) {
                method = bean.getClass().getMethod(methodName, paramClasses);
            } else {
                method = bean.getClass().getMethod(methodName);
            }
            log.info("方法验证通过: {}.{}", beanName, method.getName());
        } catch (NoSuchMethodException e) {
            // 列出可用方法供参考
            Object bean = applicationContext.getBean(beanName);
            List<String> methodSignatures = CollUtil.newArrayList();
            for (Method m : bean.getClass().getMethods()) {
                if (m.getName().equals(methodName) ||
                        m.getDeclaringClass() == bean.getClass() && m.getParameterCount() <= 4) {
                    String paramNames = StrUtil.join(",",
                            Arrays.stream(m.getParameterTypes()).map(Class::getSimpleName).toArray());
                    methodSignatures.add(m.getName() + "(" + paramNames + ")");
                }
            }
            throw new IllegalArgumentException(
                    "方法不存在或参数不匹配: [" + beanName + "." + methodName + "]。可用方法: " +
                            (CollUtil.isEmpty(methodSignatures) ? "无" : StrUtil.join(", ", methodSignatures)));
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("参数类型不存在: " + e.getMessage());
        }
    }

    /**
     * 检查任务名是否重复
     */
    private void checkDuplicateTaskName(String taskName) {
        SysScheduledTask existing = sysScheduledTaskMapper.selectByTaskName(taskName);
        if (existing != null) {
            throw new IllegalArgumentException("任务名已存在: [" + taskName + "]，请使用不同的名称或先删除已有任务");
        }
    }

    @Override
    public void refreshScheduledTasks() {
        applicationContext.publishEvent(new TaskRefreshEvent(this));
        log.info("已发布任务刷新事件");
    }

    // ============ 任务一：手动执行 & 任务二：区间补执行 ============

    /**
     * 任务一：手动执行指定ID的定时任务（立即触发一次）
     *
     * <p>复用 DynamicSchedulerConfig 的 executeTask 逻辑，直接在异步线程中执行。
     */
    @Override
    public void executeTaskManually(Long taskId) {
        SysScheduledTask task = findTaskById(taskId);
        log.info("[手动执行] 开始执行任务[{}] (ID={})", task.getTaskName(), taskId);
        taskExecutor.execute(task, TaskExecutor.SOURCE_MANUAL, LocalDateTime.now());
        log.info("[手动执行] 任务[{}] 执行完成", task.getTaskName());
    }

    /**
     * 任务二：在历史时间区间内补执行定时任务
     *
     * <p>核心逻辑（复用方法）:
     * <ol>
     *   <li>校验: 只允许过去的时间区间</li>
     *   <li>解析: 根据 cron 表达式计算出区间内所有本应触发的执行时间点</li>
     *   <li>执行: 对每个时间点异步调用 executeTask()，并动态替换 taskArgs 中的时间参数</li>
     * </ol>
     *
     * <p>示例: cron=0 0 0 * * ? (每天凌晨0点)，区间 4/15 ~ 4/16 → 触发2次 (15号0点 + 16号0点)
     */
    @Override
    public ExecutionResult executeTaskInRange(Long taskId, LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        LocalDateTime now = LocalDateTime.now();

        // 1. 区间校验：只允许过去的时间
        if (rangeEnd.isAfter(now)) {
            throw new IllegalArgumentException("区间结束时间不能是未来时间，当前=" + now + "，传入=" + rangeEnd);
        }
        if (rangeStart.isAfter(rangeEnd)) {
            throw new IllegalArgumentException("开始时间不能晚于结束时间");
        }

        // 2. 查找任务并校验必须是无参方法
        SysScheduledTask task = findTaskById(taskId);
        String taskName = task.getTaskName();
        log.info("[区间补执行] 任务[{}] ID={} | 区间: {} ~ {} | cron={}", taskName, taskId, rangeStart, rangeEnd, task.getCronExpression());

        try {
            Object bean = applicationContext.getBean(task.getBeanName());
            Method method = bean.getClass().getMethod(task.getMethodName());
            if (method.getParameterCount() > 0) {
                throw new IllegalArgumentException(
                        "区间补执行只支持无参方法，当前方法 " + task.getBeanName() + "." + task.getMethodName()
                                + " 有 " + method.getParameterCount() + " 个参数");
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("方法不存在: " + task.getBeanName() + "." + task.getMethodName());
        }

        // 3. 用 CronExpression 逐次推算区间内所有触发时间点（内联逻辑）
        List<LocalDateTime> triggerTimes = new ArrayList<>();
        CronExpression cronExpr = CronExpression.parse(task.getCronExpression());
        ZonedDateTime cursor = rangeStart.atZone(ZoneId.systemDefault());
        ZonedDateTime endZdt = rangeEnd.atZone(ZoneId.systemDefault());
        for (int i = 0; i < 10000; i++) { // 防死循环上限
            ZonedDateTime next = cronExpr.next(cursor);
            if (next == null || next.isAfter(endZdt)) break;
            LocalDateTime local = next.toLocalDateTime();
            if (!local.isBefore(rangeStart)) triggerTimes.add(local);
            cursor = next;
        }

        if (triggerTimes.isEmpty()) {
            log.info("[区间补执行] 任务[{}] 区间内无触发点", taskName);
            return new ExecutionResult(0, List.of());
        }

        log.info("[区间补执行] 任务[{}] 共 {} 个触发点: {}", taskName, triggerTimes.size(), triggerTimes);

        // 4. 逐一异步执行每个时间点的任务（无参任务直接用原始task对象，fireTime=历史触发时间点）
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (LocalDateTime fireTime : triggerTimes) {
            // capture for lambda
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    log.info("[区间补执行] [{}] 执行 {}", taskName, fireTime);
                    taskExecutor.execute(task, TaskExecutor.SOURCE_BACKFILL, fireTime);
                } catch (Exception e) {
                    log.error("[区间补执行] [{}] 执行 {} 异常: {}", taskName, fireTime, e.getMessage(), e);
                }
            }, executorService));
        }

        // 5. 等待全部完成并返回结果
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        log.info("[区间补执行] 任务[{}] 全部完成，共 {} 次 | 时间点: {}", taskName, triggerTimes.size(), triggerTimes);
        return new ExecutionResult(triggerTimes.size(), triggerTimes);
    }

    /**
     * 根据ID查找任务
     */
    private SysScheduledTask findTaskById(Long taskId) {
        SysScheduledTask task = sysScheduledTaskMapper.selectById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("定时任务不存在: ID=" + taskId);
        }
        return task;
    }

    // ============ 停止 & 启动任务（复用同一方法） ============

    @Override
    public void stopTask(Long taskId) {
        updateTaskStatus(taskId, 0, "停止");
    }

    @Override
    public void startTask(Long taskId) {
        updateTaskStatus(taskId, 1, "启动");
    }

    /**
     * 【复用方法】更新任务状态并刷新调度器
     */
    private void updateTaskStatus(Long taskId, int status, String action) {
        SysScheduledTask task = findTaskById(taskId);

        if (task.getStatus() != null && task.getStatus() == status) {
            log.info("[{}] 任务[{}] 当前已是{}状态，无需操作", action, task.getTaskName(),
                    status == 0 ? "停用" : "启用");
            return;
        }

        task.setStatus(status);
        sysScheduledTaskMapper.updateById(task);
        log.info("[{}] 任务[{}] (ID={}) 状态已改为 {}", action, task.getTaskName(), taskId, status);
        refreshScheduledTasks();
    }
}