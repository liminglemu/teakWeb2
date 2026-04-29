package com.teak.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teak.system.annotation.SnowflakeAlgorithm;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * 动态定时任务表（简化版）
 * sys_scheduled_task
 * 
 * <p>重构后只保留核心字段，移除冗余的params和parameterTypes字段，
 * 统一使用taskArgs JSON格式传递参数。
 */
@Data
@TableName(value = "sys_scheduled_task")
@EqualsAndHashCode(callSuper = true)
@ToString
public class SysScheduledTask extends BaseModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @SnowflakeAlgorithm
    private Long id;

    /**
     * 任务唯一标识
     */
    private String taskName;

    /**
     * cron表达式
     */
    private String cronExpression;

    /**
     * Spring Bean名称
     */
    private String beanName;

    /**
     * 执行方法
     */
    private String methodName;

    /**
     * 任务参数（JSON格式）
     * 
     * <p>支持两种模式：
     * 1. 位置参数模式：["value1", "value2"] - 按位置匹配方法参数
     * 2. 命名参数模式：{"param1":"value1","param2":"value2"} - 按参数名匹配（需方法参数使用@Param注解）
     * 
     * <p>示例：
     * - 无参方法：null 或 ""
     * - 位置参数：["2022-04-01", "2024-08-01"]
     * - 命名参数：{"startTime":"2022-04-01","endTime":"2024-08-01"}
     */
    private String taskArgs;

}