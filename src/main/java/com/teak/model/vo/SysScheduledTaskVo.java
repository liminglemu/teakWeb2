package com.teak.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 定时任务VO
 */
@Data
public class SysScheduledTaskVo {
    /** 任务唯一标识 */
    private String taskName;

    /** cron表达式 */
    private String cronExpression;

    /** 状态 (0-停用, 1-启用) */
    private Integer status = 1;

    // ============ 参数方式（二选一） ============

    /**
     * 方法参数列表（传统模式，需配合 parameterTypes 使用）
     * @deprecated 推荐使用 taskArgs 替代
     */
    @Deprecated
    private List<? extends Serializable> params;

    /**
     * 方法参数类型数组，逗号分隔，如: java.lang.String,java.lang.Integer
     * @deprecated 使用 taskArgs 后无需手动指定，系统自动推断
     */
    @Deprecated
    private String parameterTypes;

    /**
     * 任务参数（推荐方式）— JSON键值对格式
     * 示例: {"startTime":"2022-04-01 00:00:00","endTime":"2024-08-01 00:00:00"}
     *
     * <p>按位置顺序匹配：JSON值的顺序依次对应方法第1、第2...个参数，
     * 键名仅作标识用途（不影响实际匹配），系统自动从方法签名推断目标类型。
     */
    private Map<String, Object> taskArgs;


    /**
     * 完整方法路径，格式: beanName.methodName
     * 示例: deviceFaultRecordFetchTask.fetchDeviceFaultRecords
     * 填此项后可省略 beanName 和 methodName
     */
    private String methodPath;
}
