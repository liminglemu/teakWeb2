package com.teak.service;

import com.teak.model.SysScheduledTask;
import com.teak.model.vo.SysScheduledTaskVo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务管理器接口
 */
public interface ScheduledTaskManager {

    /**
     * 添加并启动定时任务
     */
    void addAndStartScheduledTask(SysScheduledTaskVo sysScheduledTaskVo);

    /**
     * 刷新所有定时任务
     */
    void refreshScheduledTasks();

    /**
     * 获取所有任务
     */
    List<SysScheduledTask> getAllTasks();

    /**
     * 手动执行指定ID的定时任务（立即触发一次）
     *
     * @param taskId 任务主键ID
     */
    void executeTaskManually(Long taskId);

    /**
     * 在历史时间区间内补执行定时任务
     *
     * <p>根据任务的 cron 表达式，计算出区间内所有本应触发的执行时间点，
     * 然后逐一异步执行。仅支持过去的区间。
     *
     * @param taskId  任务主键ID
     * @param rangeStart 区间开始时间（含）
     * @param rangeEnd   区间结束时间（含）
     * @return 实际触发的执行次数及各次执行时间
     */
    ExecutionResult executeTaskInRange(Long taskId, LocalDateTime rangeStart, LocalDateTime rangeEnd);

    /**
     * 停止定时任务（状态改为0并刷新调度器）
     *
     * @param taskId 任务主键ID
     */
    void stopTask(Long taskId);

    /**
     * 启动定时任务（状态改为1并刷新调度器）
     *
     * @param taskId 任务主键ID
     */
    void startTask(Long taskId);

    /**
     * 区间执行结果
     */
    record ExecutionResult(int triggerCount, List<LocalDateTime> triggeredTimes) {
    }
}