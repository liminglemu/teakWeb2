ALTER TABLE sys_scheduled_task
    ADD COLUMN task_args TEXT DEFAULT NULL COMMENT '任务参数(JSON键值对格式)，如: {"startTime":"2022-04-01 00:00:00","endTime":"2024-08-01 00:00:00"}';
