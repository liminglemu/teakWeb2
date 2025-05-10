package com.teak.business.service;


import com.teak.business.model.SysScheduledTask;
import com.teak.business.model.vo.SysScheduledTaskVo;

import java.util.List;

/**
 * Created with: IntelliJ IDEA
 *
 * @Author: li zheng
 * @Date: 2025/4/19 10:19
 * @Project: teakWeb
 * @File: SysScheduledTaskService.java
 * @Description:
 */
public interface SysScheduledTaskService {
    List<SysScheduledTask> getAllTask();

    void addScheduledTask(SysScheduledTaskVo sysScheduledTaskVo);

    List<SysScheduledTask>  findByStatus(int i);
}
