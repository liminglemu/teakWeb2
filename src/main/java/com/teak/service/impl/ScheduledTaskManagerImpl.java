package com.teak.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teak.mapper.SysScheduledTaskMapper;
import com.teak.model.SysScheduledTask;
import com.teak.model.vo.SysScheduledTaskVo;
import com.teak.service.ScheduledTaskManager;
import com.teak.system.event.TaskRefreshEvent;
import com.teak.system.utils.TeakUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Override
    public List<SysScheduledTask> getAllTasks() {
        return sysScheduledTaskMapper.getAllTask();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addAndStartScheduledTask(SysScheduledTaskVo sysScheduledTaskVo) {
        SysScheduledTask scheduledTask = new SysScheduledTask();
        teakUtils.copyProperties(sysScheduledTaskVo, scheduledTask);
        scheduledTask.setBeanName(teakUtils.lowerFirstCharAndTrim(sysScheduledTaskVo.getBeanName()));
        scheduledTask.setMethodName(teakUtils.lowerFirstCharAndTrim(sysScheduledTaskVo.getMethodName()));
        scheduledTask.setParameterTypes(teakUtils.resolveReferenceClassName(sysScheduledTaskVo.getParameterTypes()));
        if (sysScheduledTaskVo.getParams() != null) {
            try {
                // 修复参数序列化问题，直接存储参数而不是嵌套列表
                scheduledTask.setParams(new ObjectMapper().writeValueAsString(sysScheduledTaskVo.getParams()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        sysScheduledTaskMapper.insert(scheduledTask);
        log.info("成功添加定时任务: {}", scheduledTask.getTaskName());

        // 刷新定时任务
        refreshScheduledTasks();
    }

    @Override
    public void refreshScheduledTasks() {
        // 发布任务刷新事件，触发定时任务重新加载
        applicationContext.publishEvent(new TaskRefreshEvent(this));
        log.info("已发布任务刷新事件");
    }
}