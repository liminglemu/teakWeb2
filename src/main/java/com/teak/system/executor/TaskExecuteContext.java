package com.teak.system.executor;

import java.time.LocalDateTime;

/**
 * 定时任务执行上下文（ThreadLocal）
 *
 * <p>用途：业务方法内部可通过静态方法获取当前定时任务的触发执行时间（fireTime），
 * 无需修改方法签名即可使用。
 *
 * <h3>两种使用方式：</h3>
 * <ol>
 *   <li><b>自动注入（推荐）</b>：目标方法声明 {@code LocalDateTime} 参数，执行器自动填入 fireTime</li>
 *   <li><b>手动获取</b>：在业务代码中调用 {@code TaskExecuteContext.getFireTime()} 获取</li>
 * </ol>
 *
 * <pre>
 * // 示例：手动获取
 * public void syncData() {
 *     LocalDateTime fireTime = TaskExecuteContext.getFireTime();
 *     entity.setExecTime(fireTime);  // 写入业务表
 *     mapper.insert(entity);
 * }
 * </pre>
 */
public final class TaskExecuteContext {

    private TaskExecuteContext() {
    }

    /** 当前线程的触发执行时间 */
    private static final ThreadLocal<LocalDateTime> FIRE_TIME_HOLDER = new ThreadLocal<>();

    /** 当前线程的任务名称 */
    private static final ThreadLocal<String> TASK_NAME_HOLDER = new ThreadLocal<>();

    /**
     * 设置上下文（TaskExecutor 在 invoke 前调用）
     */
    static void set(LocalDateTime fireTime, String taskName) {
        FIRE_TIME_HOLDER.set(fireTime);
        TASK_NAME_HOLDER.set(taskName);
    }

    /**
     * 清理上下文（TaskExecutor 在 finally 中调用，防止内存泄漏）
     */
    static void clear() {
        FIRE_TIME_HOLDER.remove();
        TASK_NAME_HOLDER.remove();
    }

    /**
     * 获取当前定时任务的触发执行时间
     *
     * @return fireTime，若不在任务执行上下文中返回 null
     */
    public static LocalDateTime getFireTime() {
        return FIRE_TIME_HOLDER.get();
    }

    /**
     * 获取当前执行的任务名称
     *
     * @return taskName，若不在任务执行上下文中返回 null
     */
    public static String getTaskName() {
        return TASK_NAME_HOLDER.get();
    }

    /**
     * 判断是否处于定时任务执行上下文中
     */
    public static boolean isInTaskContext() {
        return FIRE_TIME_HOLDER.get() != null;
    }
}
