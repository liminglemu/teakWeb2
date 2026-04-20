package com.teak.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teak.system.annotation.SnowflakeAlgorithm;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 定时任务执行记录表
 * sys_scheduled_task_log
 */
@Data
@TableName(value = "sys_scheduled_task_log")
@EqualsAndHashCode(callSuper = true)
@ToString
public class SysScheduledTaskLog extends BaseModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键(雪花ID)
     */
    @SnowflakeAlgorithm
    private Long id;

    /**
     * 关联的任务ID (sys_scheduled_task.id)
     */
    private Long taskId;

    /**
     * 任务名称(冗余，方便查询)
     */
    private String taskName;

    /**
     * 触发执行时间（调度器计划时间 / 区间补执行时为历史时间点）
     */
    private LocalDateTime fireTime;

    /**
     * 触发来源:
     * SCHEDULED - 正常调度触发
     * MANUAL    - 手动执行
     * BACKFILL  - 区间补执行
     */
    private String triggerSource;

    /**
     * 执行状态: 0-运行中, 1-成功, 2-失败
     */
    private Integer status;

    /**
     * 错误信息(失败时)
     */
    private String errorMessage;

    /**
     * 执行耗时(毫秒)
     */
    private Long costMs;
}
