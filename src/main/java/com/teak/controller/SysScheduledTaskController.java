package com.teak.controller;

import com.teak.model.SysScheduledTask;
import com.teak.model.vo.SysScheduledTaskVo;
import com.teak.service.ScheduledTaskManager;
import com.teak.system.result.GlobalResult;
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
public class SysScheduledTaskController {

    private final ScheduledTaskManager scheduledTaskManager;

    @GetMapping("/getAllTask")
    public GlobalResult getAllTask() {
        List<SysScheduledTask> allTask = scheduledTaskManager.getAllTasks();
        return GlobalResult.success(allTask);
    }

    @PostMapping("/addScheduledTask")
    public GlobalResult addScheduledTask(@RequestBody SysScheduledTaskVo sysScheduledTaskVo) {
        scheduledTaskManager.addAndStartScheduledTask(sysScheduledTaskVo);
        return GlobalResult.success(null);
    }

    @PostMapping("/refreshScheduledTasks")
    public GlobalResult refreshScheduledTasks() {
        scheduledTaskManager.refreshScheduledTasks();
        return GlobalResult.success("定时任务刷新成功");
    }

}