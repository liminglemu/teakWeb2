package com.teak.system.event;

import org.springframework.context.ApplicationEvent;

/**
 * 任务刷新事件
 */
public class TaskRefreshEvent extends ApplicationEvent {
    
    public TaskRefreshEvent(Object source) {
        super(source);
    }
}