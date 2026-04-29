package com.teak.system.executor;

import cn.hutool.core.text.CharSequenceUtil;
import com.teak.core.task.TaskInvoker;
import com.teak.mapper.SysScheduledTaskLogMapper;
import com.teak.model.SysScheduledTask;
import com.teak.model.SysScheduledTaskLog;
import com.teak.system.exception.TaskExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 定时任务执行器 — 负责单次任务的完整执行生命周期（简化版）
 *
 * <p>重构目标：简化反射调用逻辑，使用统一的TaskInvoker
 * <p>职责: 日志记录 → TaskInvoker调用 → 结果记录
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskExecutor {

    private final TaskInvoker taskInvoker;
    private final SysScheduledTaskLogMapper taskLogMapper;

    /** 触发来源常量 */
    public static final String SOURCE_SCHEDULED = "SCHEDULED";
    public static final String SOURCE_MANUAL = "MANUAL";
    public static final String SOURCE_BACKFILL = "BACKFILL";

    /**
     * 执行单个定时任务（简化版）
     *
     * <p>完整生命周期:
     * <ol>
     *   <li>插入执行记录 (status=0 运行中)</li>
     *   <li>使用TaskInvoker调用业务方法</li>
     *   <li>更新执行记录 (1成功/2失败 + costMs)</li>
     * </ol>
     */
    public void execute(SysScheduledTask task, String triggerSource, LocalDateTime fireTime) {
        String taskName = task.getTaskName();
        long startTime = System.currentTimeMillis();

        // 设置 ThreadLocal 上下文
        TaskExecuteContext.set(fireTime, taskName);

        // 1. 写入执行记录
        SysScheduledTaskLog logEntity = createAndSaveLog(task, triggerSource, fireTime);
        log.info("[{}][logId={}] 开始执行 | source={} | fireTime={}",
                taskName, logEntity.getId(), triggerSource, fireTime);

        try {
            // 2. 调用业务方法
            taskInvoker.invoke(task.getBeanName(), task.getMethodName(), task.getTaskArgs());

            // 3. 成功
            finishLog(logEntity, startTime, true, null);
            log.info("[{}][logId={}] 执行完成 | cost={}ms", taskName, logEntity.getId(),
                    System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            String errorMsg = e.getMessage();
            finishLog(logEntity, startTime, false, errorMsg);
            log.error("[{}][logId={}] 执行失败 | cost={}ms | error={}", taskName, logEntity.getId(),
                    System.currentTimeMillis() - startTime, errorMsg, e);
            throw new TaskExecutionException("任务 [" + taskName + "] 执行失败: " + errorMsg, e);
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
}