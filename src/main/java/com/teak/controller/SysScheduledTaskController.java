package com.teak.controller;

import com.teak.model.SysScheduledTask;
import com.teak.model.vo.SysScheduledTaskVo;
import com.teak.result.GlobalResult;
import com.teak.service.SysScheduledTaskService;
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

    private final SysScheduledTaskService sysScheduledTaskService;

    @GetMapping("/getAllTask")
    public GlobalResult getAllTask() {
        List<SysScheduledTask> allTask = sysScheduledTaskService.getAllTask();
        return GlobalResult.success(allTask);
    }

    @PostMapping("/addScheduledTask")
    public GlobalResult addScheduledTask(@RequestBody SysScheduledTaskVo sysScheduledTaskVo) {
        sysScheduledTaskService.addScheduledTask(sysScheduledTaskVo);
        return GlobalResult.success(null);
    }

}
