package com.teak.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.teak.core.task.TaskInvoker;
import com.teak.mapper.SysScheduledTaskMapper;
import com.teak.model.SysScheduledTask;
import com.teak.model.vo.SysScheduledTaskVo;
import com.teak.service.ScheduledTaskManager;
import com.teak.system.event.TaskRefreshEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * 简化版定时任务管理器
 *
 * <p>重构目标：高效、添加简洁、使用方便
 * <p>设计原则：
 * 1. 统一使用taskArgs JSON格式传递参数
 * 2. 在任务添加时进行方法验证，执行时直接调用
 * 3. 简化区间补执行逻辑
 * 4. 移除复杂的传统参数模式支持
 */
@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class SimpleScheduledTaskManager implements ScheduledTaskManager {

    private final SysScheduledTaskMapper taskMapper;
    private final ApplicationContext applicationContext;
    private final TaskInvoker taskInvoker;
    private final ExecutorService executorService;

    @Override
    public List<SysScheduledTask> getAllTasks() {
        return taskMapper.getAllTask();
    }

    /**
     * 添加并启动定时任务（简化版）
     *
     * <p>只支持methodPath格式：beanName.methodName
     * 只支持taskArgs参数格式：JSON对象或数组
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAndStartScheduledTask(SysScheduledTaskVo vo) throws Exception {
        // 1. 基本校验
        if (CharSequenceUtil.isBlank(vo.getTaskName())) {
            throw new IllegalArgumentException("任务名称不能为空");
        }
        if (CharSequenceUtil.isBlank(vo.getCronExpression())) {
            throw new IllegalArgumentException("cron表达式不能为空");
        }
        if (CharSequenceUtil.isBlank(vo.getMethodPath())) {
            throw new IllegalArgumentException("方法路径不能为空，格式: beanName.methodName");
        }

        // 2. 解析methodPath
        String methodPathStr = vo.getMethodPath();
        String path = methodPathStr.trim();
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex <= 0 || dotIndex >= path.length() - 1) {
            throw new IllegalArgumentException("methodPath格式错误，应为: beanName.methodName");
        }

        String beanName = path.substring(0, dotIndex);
        String methodName = path.substring(dotIndex + 1);

        // 简单验证bean是否存在
        if (!applicationContext.containsBean(beanName)) {
            throw new IllegalArgumentException("Bean不存在: " + beanName);
        }

        MethodPath methodPath = new MethodPath(beanName, methodName);

        // 3. 校验cron表达式
        try {
            CronExpression.parse(vo.getCronExpression());
        } catch (Exception e) {
            throw new IllegalArgumentException("cron表达式格式错误: " + e.getMessage());
        }

        // 4. 校验任务名唯一
        SysScheduledTask existing = taskMapper.selectByTaskName(vo.getTaskName());
        if (existing != null) {
            throw new IllegalArgumentException("任务名已存在: " + vo.getTaskName());
        }

        // 5. 验证方法存在且参数匹配
        String taskArgsJson = convertTaskArgsToJson(vo.getTaskArgs());
        taskInvoker.validateMethod(methodPath.beanName, methodPath.methodName, taskArgsJson);

        // 6. 保存任务
        SysScheduledTask task = new SysScheduledTask();
        task.setTaskName(vo.getTaskName());
        task.setCronExpression(vo.getCronExpression());
        task.setBeanName(methodPath.beanName);
        task.setMethodName(methodPath.methodName);
        task.setTaskArgs(taskArgsJson);
        task.setStatus(vo.getStatus() != null ? vo.getStatus() : 1);

        taskMapper.insert(task);
        log.info("添加定时任务成功: {} [{}.{}]", vo.getTaskName(), methodPath.beanName, methodPath.methodName);

        // 7. 刷新调度器
        refreshScheduledTasks();
    }

    /**
     * 手动执行任务
     */
    @Override
    public void executeTaskManually(Long taskId) {
        SysScheduledTask task = findTaskById(taskId);
        log.info("[手动执行] 开始执行任务: {}", task.getTaskName());

        CompletableFuture.runAsync(() -> {
            try {
                taskInvoker.invoke(task.getBeanName(), task.getMethodName(), task.getTaskArgs());
                log.info("[手动执行] 任务执行完成: {}", task.getTaskName());
            } catch (Exception e) {
                log.error("[手动执行] 任务执行失败: {}", task.getTaskName(), e);
            }
        }, executorService);
    }

    /**
     * 区间补执行（简化版）
     *
     * <p>只支持无参方法的区间补执行，避免参数时间替换的复杂性
     * 后续可以扩展支持带时间参数的方法
     */
    @Override
    public ExecutionResult executeTaskInRange(Long taskId, LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        // 1. 基本校验
        LocalDateTime now = LocalDateTime.now();
        if (rangeEnd.isAfter(now)) {
            throw new IllegalArgumentException("区间结束时间不能是未来时间");
        }
        if (rangeStart.isAfter(rangeEnd)) {
            throw new IllegalArgumentException("开始时间不能晚于结束时间");
        }

        // 2. 查找任务
        SysScheduledTask task = findTaskById(taskId);

        // 3. 检查是否为无参方法（简化逻辑，只支持无参）
        if (StrUtil.isNotBlank(task.getTaskArgs())) {
            throw new IllegalArgumentException("区间补执行目前只支持无参方法");
        }

        // 4. 计算触发时间点
        List<LocalDateTime> triggerTimes = calculateTriggerTimes(task.getCronExpression(), rangeStart, rangeEnd);
        if (triggerTimes.isEmpty()) {
            log.info("[区间补执行] 任务[{}] 区间内无触发点", task.getTaskName());
            return new ExecutionResult(0, List.of());
        }

        log.info("[区间补执行] 任务[{}] 共 {} 个触发点", task.getTaskName(), triggerTimes.size());

        // 5. 异步执行
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (LocalDateTime fireTime : triggerTimes) {
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    log.debug("[区间补执行] 执行 {} 时间点: {}", task.getTaskName(), fireTime);
                    taskInvoker.invoke(task.getBeanName(), task.getMethodName(), task.getTaskArgs());
                } catch (Exception e) {
                    log.error("[区间补执行] 执行失败: {} {}", task.getTaskName(), fireTime, e);
                }
            }, executorService));
        }

        // 6. 等待完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return new ExecutionResult(triggerTimes.size(), triggerTimes);
    }

    @Override
    public void stopTask(Long taskId) {
        updateTaskStatus(taskId, 0, "停止");
    }

    @Override
    public void startTask(Long taskId) {
        updateTaskStatus(taskId, 1, "启动");
    }

    @Override
    public void refreshScheduledTasks() {
        applicationContext.publishEvent(new TaskRefreshEvent(this));
        log.info("已发布任务刷新事件");
    }

    // ============ 私有方法 ============

    private String convertTaskArgsToJson(Object taskArgs) {
        if (ObjectUtil.isNull(taskArgs)) {
            return null;
        }
        return JSONUtil.toJsonStr(taskArgs);
    }

    private List<LocalDateTime> calculateTriggerTimes(String cronExpression, LocalDateTime start, LocalDateTime end) {
        List<LocalDateTime> triggerTimes = new ArrayList<>();
        try {
            CronExpression cronExpr = CronExpression.parse(cronExpression);
            ZonedDateTime cursor = start.atZone(ZoneId.systemDefault());
            ZonedDateTime endZdt = end.atZone(ZoneId.systemDefault());

            for (int i = 0; i < 10000; i++) { // 防死循环
                ZonedDateTime next = cronExpr.next(cursor);
                if (next == null || next.isAfter(endZdt)) break;
                LocalDateTime local = next.toLocalDateTime();
                if (!local.isBefore(start)) triggerTimes.add(local);
                cursor = next;
            }
        } catch (Exception e) {
            log.error("计算触发时间失败", e);
        }
        return triggerTimes;
    }

    private SysScheduledTask findTaskById(Long taskId) {
        SysScheduledTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("定时任务不存在: ID=" + taskId);
        }
        return task;
    }

    private void updateTaskStatus(Long taskId, int status, String action) {
        SysScheduledTask task = findTaskById(taskId);

        if (task.getStatus() != null && task.getStatus() == status) {
            log.info("[{}] 任务[{}] 当前已是{}状态，无需操作", action, task.getTaskName(),
                    status == 0 ? "停用" : "启用");
            return;
        }

        task.setStatus(status);
        taskMapper.updateById(task);
        log.info("[{}] 任务[{}] 状态已改为 {}", action, task.getTaskName(), status);
        refreshScheduledTasks();
    }

    // ============ 内部类 ============

    private record MethodPath(String beanName, String methodName) {
    }
}