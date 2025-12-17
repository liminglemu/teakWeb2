package com.teak.service;

import com.teak.model.SysScheduledTask;
import com.teak.model.vo.SysScheduledTaskVo;

import java.util.List;

/**
 * 定时任务管理器接口
 */
public interface ScheduledTaskManager {

    /**
     * 添加并启动定时任务
     *
     * @param sysScheduledTaskVo 任务信息
     */
    void addAndStartScheduledTask(SysScheduledTaskVo sysScheduledTaskVo);

    /**
     * 刷新所有定时任务
     */
    void refreshScheduledTasks();

    /**
     * 获取所有任务
     *
     * @return 任务列表
     */
    List<SysScheduledTask> getAllTasks();
}