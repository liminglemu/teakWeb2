package com.teak.controller;

import com.teak.model.SysScheduledTask;
import com.teak.model.vo.SysScheduledTaskVo;
import com.teak.service.ScheduledTaskManager;
import com.teak.system.result.GlobalResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created with: IntelliJ IDEA
 *
 * @Author: li zheng
 * @Date: 2025/4/19 09:39
 * @Project: teakWeb
 * @File: SysScheduledTaskController.java
 * @Description: 定时任务控制器
 */
@RestController
@RequestMapping("/api/sysScheduledTask")
@RequiredArgsConstructor
@Tag(name = "定时任务", description = "定时任务管理相关接口")
public class SysScheduledTaskController {

    private final ScheduledTaskManager scheduledTaskManager;

    @GetMapping("/getAllTask")
    @Operation(summary = "获取所有任务", description = "获取所有定时任务")
    public GlobalResult getAllTask() {
        List<SysScheduledTask> allTask = scheduledTaskManager.getAllTasks();
        return GlobalResult.success(allTask);
    }

    @PostMapping("/addScheduledTask")
    @Operation(summary = "添加定时任务", description = "添加并启动新的定时任务")
    public GlobalResult addScheduledTask(@Parameter(description = "定时任务信息") @RequestBody SysScheduledTaskVo sysScheduledTaskVo) {
        scheduledTaskManager.addAndStartScheduledTask(sysScheduledTaskVo);
        return GlobalResult.success(null);
    }

    @PostMapping("/refreshScheduledTasks")
    @Operation(summary = "刷新定时任务", description = "刷新定时任务列表")
    public GlobalResult refreshScheduledTasks() {
        scheduledTaskManager.refreshScheduledTasks();
        return GlobalResult.success("定时任务刷新成功");
    }

}