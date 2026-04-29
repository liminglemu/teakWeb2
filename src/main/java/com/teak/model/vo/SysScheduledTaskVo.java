package com.teak.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 定时任务VO（简化版）
 */
@Data
public class SysScheduledTaskVo {
    /** 任务唯一标识 */
    private String taskName;

    /** cron表达式 */
    private String cronExpression;

    /** 状态 (0-停用, 1-启用) */
    private Integer status = 1;

    // ============ 参数方式（统一使用taskArgs） ============

    /**
     * 任务参数（JSON格式）
     * 
     * <p>支持两种模式：
     * 1. 位置参数：使用List格式，如 ["value1", "value2"]
     * 2. 命名参数：使用Map格式，如 {"param1":"value1","param2":"value2"}
     * 
     * <p>命名参数需要方法参数使用@Param注解，如：
     * {@code public void myMethod(@Param("param1") String p1, @Param("param2") String p2)}
     * 
     * <p>存储时序列化为JSON字符串，执行时由TaskInvoker自动解析和类型转换。
     */
    private Object taskArgs;


    /**
     * 完整方法路径，格式: beanName.methodName
     * 示例: deviceFaultRecordFetchTask.fetchDeviceFaultRecords
     * 填此项后可省略 beanName 和 methodName
     */
    private String methodPath;
}
