package com.teak.controller;

import com.teak.mapper.SysScheduledTaskLogMapper;
import com.teak.model.SysScheduledTask;
import com.teak.model.SysScheduledTaskLog;
import com.teak.model.vo.SysScheduledTaskVo;
import com.teak.service.ScheduledTaskManager;
import com.teak.system.result.GlobalResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务控制器
 */
@RestController
@RequestMapping("/api/sysScheduledTask")
@RequiredArgsConstructor
@Tag(name = "定时任务", description = "定时任务管理相关接口")
public class SysScheduledTaskController {

    private final ScheduledTaskManager scheduledTaskManager;
    private final SysScheduledTaskLogMapper scheduledTaskLogMapper;

    @GetMapping("/getAllTask")
    @Operation(summary = "获取所有任务", description = "获取所有定时任务")
    public GlobalResult getAllTask() {
        List<SysScheduledTask> allTask = scheduledTaskManager.getAllTasks();
        return GlobalResult.success(allTask);
    }

    @PostMapping("/addScheduledTask")
    @Operation(summary = "添加定时任务（完整版）", description = "添加并启动新的定时任务，支持完整配置。" +
            "推荐使用 taskArgs 字段传递参数（JSON键值对，自动推断类型），" +
            "也可继续使用传统 params + parameterTypes 方式。")
    public GlobalResult addScheduledTask(@Parameter(description = "定时任务信息") @RequestBody SysScheduledTaskVo sysScheduledTaskVo) {
        try {
            scheduledTaskManager.addAndStartScheduledTask(sysScheduledTaskVo);
            return GlobalResult.success(null, "定时任务添加成功");
        } catch (IllegalArgumentException e) {
            return GlobalResult.error(e.getMessage());
        } catch (Exception e) {
            return GlobalResult.error("添加失败: " + e.getMessage());
        }
    }

    /**
     * 快速添加定时任务（只需3个字段，无参数版）
     */
    @PostMapping("/quickAdd")
    @Operation(
            summary = "快速添加定时任务(无参)",
            description = "简化版：只需任务名、方法路径、cron表达式。适用于无参方法或默认行为。方法路径格式: beanName.methodName"
    )
    public GlobalResult quickAdd(
            @Parameter(description = "任务名称") @RequestParam String taskName,
            @Parameter(description = "方法路径，如: deviceFaultRecordFetchTask.fetchDeviceFaultRecords") @RequestParam String methodPath,
            @Parameter(description = "cron表达式，如: 0 */5 * * * ? （每5分钟）") @RequestParam String cronExpression
    ) {
        try {
            SysScheduledTaskVo vo = new SysScheduledTaskVo();
            vo.setTaskName(taskName);
            vo.setMethodPath(methodPath);
            vo.setCronExpression(cronExpression);
            vo.setStatus(1);
            scheduledTaskManager.addAndStartScheduledTask(vo);
            return GlobalResult.success(null, "定时任务 [" + taskName + "] 添加成功");
        } catch (IllegalArgumentException e) {
            return GlobalResult.error(e.getMessage());
        } catch (Exception e) {
            return GlobalResult.error("添加失败: " + e.getMessage());
        }
    }

    /**
     * 快速添加带参数定时任务（推荐方式）
     *
     * <p>示例请求:
     * <pre>{@code
     * {
     *   "taskName": "设备故障记录定时采集",
     *   "methodPath": "deviceFaultRecordsServiceImpl.getDeviceFaultRecords",
     *   "cronExpression": "0 * /5 * * * ?",
     *   "taskArgs": {
     *     "startTime": "2022-04-01 00:00:00",
     *     "endTime": "2024-08-01 00:00:00"
     *   }
     * }
     * }</pre>
     */
    @PostMapping("/quickAddWithArgs")
    @Operation(
            summary = "快速添加带参定时任务(推荐)",
            description = "最简方式：只需4个字段(taskName, methodPath, cronExpression, taskArgs)。" +
                    "taskArgs为JSON键值对，按值顺序自动匹配方法参数位置，系统自动推断类型，无需填写parameterTypes。"
    )
    public GlobalResult quickAddWithArgs(@RequestBody SysScheduledTaskVo vo) {
        try {
            scheduledTaskManager.addAndStartScheduledTask(vo);
            return GlobalResult.success(null, "定时任务 [" + vo.getTaskName() + "] 添加成功，参数自动匹配完成");
        } catch (IllegalArgumentException e) {
            return GlobalResult.error(e.getMessage());
        } catch (Exception e) {
            return GlobalResult.error("添加失败: " + e.getMessage());
        }
    }

    @PostMapping("/refreshScheduledTasks")
    @Operation(summary = "刷新定时任务", description = "刷新定时任务列表")
    public GlobalResult refreshScheduledTasks() {
        scheduledTaskManager.refreshScheduledTasks();
        return GlobalResult.success("定时任务刷新成功");
    }

    /**
     * 获取常用 cron 表达式参考
     */
    @GetMapping("/cronPresets")
    @Operation(summary = "获取常用Cron表达式", description = "返回常用周期的cron表达式供选择")
    public GlobalResult getCronPresets() {
        List<Object> presets = List.of(
                new CronPreset("每分钟", "0 * * * * ?"),
                new CronPreset("每5分钟", "0 */5 * * * ?"),
                new CronPreset("每10分钟", "0 */10 * * * ?"),
                new CronPreset("每30分钟", "0 */30 * * * ?"),
                new CronPreset("每小时", "0 0 * * * ?"),
                new CronPreset("每天凌晨0点", "0 0 0 * * ?"),
                new CronPreset("每天早上8点", "0 0 8 * * ?"),
                new CronPreset("每周一早上9点", "0 0 9 ? * MON"),
                new CronPreset("每月1号凌晨0点", "0 0 0 1 * ?"),
                new CronPreset("工作日(周一到周五)每小时", "0 0 * * ? MON-FRI"),
                new CronPreset("每秒", "* * * * * *")
        );
        return GlobalResult.success(presets);
    }

    /**
     * cron 预设内部类
     */
    private record CronPreset(String label, String expression) {
    }

    // ==================== 任务一：手动执行 ====================

    @PostMapping("/execute/{id}")
    @Operation(summary = "手动执行定时任务", description = "立即触发执行指定ID的定时任务一次（异步）")
    public GlobalResult executeManually(@Parameter(description = "任务ID") @PathVariable Long id) {
        try {
            scheduledTaskManager.executeTaskManually(id);
            return GlobalResult.success(null, "任务已触发执行");
        } catch (IllegalArgumentException e) {
            return GlobalResult.error(e.getMessage());
        } catch (Exception e) {
            return GlobalResult.error("执行失败: " + e.getMessage());
        }
    }

    // ==================== 停止 & 启动 ====================

    @PostMapping("/stop/{id}")
    @Operation(summary = "停止定时任务", description = "将指定任务状态改为0（停用），并刷新调度器使变更立即生效")
    public GlobalResult stopTask(@Parameter(description = "任务ID") @PathVariable Long id) {
        try {
            scheduledTaskManager.stopTask(id);
            return GlobalResult.success(null, "任务已停止");
        } catch (IllegalArgumentException e) {
            return GlobalResult.error(e.getMessage());
        } catch (Exception e) {
            return GlobalResult.error("停止失败: " + e.getMessage());
        }
    }

    @PostMapping("/start/{id}")
    @Operation(summary = "启动定时任务", description = "将指定任务状态改为1（启用），并刷新调度器使变更立即生效")
    public GlobalResult startTask(@Parameter(description = "任务ID") @PathVariable Long id) {
        try {
            scheduledTaskManager.startTask(id);
            return GlobalResult.success(null, "任务已启动");
        } catch (IllegalArgumentException e) {
            return GlobalResult.error(e.getMessage());
        } catch (Exception e) {
            return GlobalResult.error("启动失败: " + e.getMessage());
        }
    }

    @PostMapping("/executeInRange")
    @Operation(
            summary = "区间补执行定时任务",
            description = "在指定的过去时间区间内，根据cron表达式计算所有本应触发的执行时间点并逐一补执行。" +
                    "仅支持过去的日期范围。系统会自动将taskArgs中的时间参数替换为当天对应的时间值。"
    )
    public GlobalResult executeInRange(
            @Parameter(description = "任务ID") @RequestParam Long id,
            @Parameter(description = "区间开始时间(含), 格式: yyyy-MM-dd HH:mm:ss") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "区间结束时间(含), 格式: yyyy-MM-dd HH:mm:ss，不能是未来时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime
    ) {
        try {
            ScheduledTaskManager.ExecutionResult result = scheduledTaskManager.executeTaskInRange(id, startTime, endTime);
            if (result.triggerCount() == 0) {
                return GlobalResult.success(result, "该区间内无触发时间点");
            }
            return GlobalResult.success(result,
                    String.format("补执行完成: 共触发 %d 次 | 时间点: %s",
                            result.triggerCount(), result.triggeredTimes()));
        } catch (IllegalArgumentException e) {
            return GlobalResult.error(e.getMessage());
        } catch (Exception e) {
            return GlobalResult.error("补执行失败: " + e.getMessage());
        }
    }

    // ==================== 执行记录查询 ====================

    @GetMapping("/log/list")
    @Operation(summary = "查询任务执行记录", description = "根据任务ID查询该任务的执行历史记录，按触发时间倒序")
    public GlobalResult getExecutionLogs(
            @Parameter(description = "任务ID（必传）") @RequestParam Long taskId,
            @Parameter(description = "区间开始时间(可选), 格式: yyyy-MM-dd HH:mm:ss") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "区间结束时间(可选), 格式: yyyy-MM-dd HH:mm:ss") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        List<SysScheduledTaskLog> logs;
        if (startTime != null || endTime != null) {
            logs = scheduledTaskLogMapper.selectByTaskIdAndTimeRange(taskId, startTime, endTime);
        } else {
            logs = scheduledTaskLogMapper.selectByTaskId(taskId);
        }
        return GlobalResult.success(logs);
    }

    @GetMapping("/log/recent")
    @Operation(summary = "查询最近N条执行记录", description = "全局最近的任务执行记录，跨任务查看")
    public GlobalResult getRecentLogs(
            @Parameter(description = "条数，默认20") @RequestParam(defaultValue = "20") int limit) {
        return GlobalResult.success(scheduledTaskLogMapper.selectRecent(limit));
    }

    @GetMapping("/log/{id}")
    @Operation(summary = "查询单条执行记录详情", description = "根据执行记录ID查详情（含fireTime、耗时、错误信息等）")
    public GlobalResult getLogDetail(@Parameter(description = "执行记录ID") @PathVariable Long id) {
        SysScheduledTaskLog log = scheduledTaskLogMapper.selectById(id);
        if (log == null) {
            return GlobalResult.error("执行记录不存在: ID=" + id);
        }
        return GlobalResult.success(log);
    }
}