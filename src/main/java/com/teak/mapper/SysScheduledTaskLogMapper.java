package com.teak.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teak.model.SysScheduledTaskLog;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务执行记录 Mapper
 */
@Mapper
public interface SysScheduledTaskLogMapper extends BaseMapper<SysScheduledTaskLog> {

    /**
     * 根据任务ID查询执行记录（按 fire_time 倒序，最新在前）
     */
    default List<SysScheduledTaskLog> selectByTaskId(Long taskId) {
        return selectList(new LambdaQueryWrapper<SysScheduledTaskLog>()
                .eq(SysScheduledTaskLog::getTaskId, taskId)
                .orderByDesc(SysScheduledTaskLog::getFireTime));
    }

    /**
     * 根据任务ID + 时间范围查询执行记录
     */
    default List<SysScheduledTaskLog> selectByTaskIdAndTimeRange(Long taskId, LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<SysScheduledTaskLog> wrapper = new LambdaQueryWrapper<SysScheduledTaskLog>()
                .eq(SysScheduledTaskLog::getTaskId, taskId)
                .ge(start != null, SysScheduledTaskLog::getFireTime, start)
                .le(end != null, SysScheduledTaskLog::getFireTime, end)
                .orderByDesc(SysScheduledTaskLog::getFireTime);
        return selectList(wrapper);
    }

    /**
     * 查询最近N条执行记录
     */
    default List<SysScheduledTaskLog> selectRecent(int limit) {
        return selectList(new LambdaQueryWrapper<SysScheduledTaskLog>()
                .orderByDesc(SysScheduledTaskLog::getCreateTime)
                .last("LIMIT " + limit));
    }
}
