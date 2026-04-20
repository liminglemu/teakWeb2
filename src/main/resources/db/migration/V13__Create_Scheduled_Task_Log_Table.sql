CREATE TABLE `sys_scheduled_task_log` (
  `id`            BIGINT       NOT NULL COMMENT '主键(雪花ID)',
  `task_id`       BIGINT       NOT NULL COMMENT '关联的任务ID (sys_scheduled_task.id)',
  `task_name`     VARCHAR(255) NOT NULL COMMENT '任务名称(冗余，方便查询)',
  `fire_time`     DATETIME     NOT NULL COMMENT '触发执行时间（调度器计划时间/区间补执行=历史时间）',
  `trigger_source` VARCHAR(32)  NOT NULL DEFAULT 'SCHEDULED' COMMENT '触发来源: SCHEDULED-正常调度, MANUAL-手动执行, BACKFILL-区间补执行',
  `status`        TINYINT      NOT NULL DEFAULT 0 COMMENT '执行状态: 0-运行中, 1-成功, 2-失败',
  `error_message` TEXT         DEFAULT NULL COMMENT '错误信息(失败时)',
  `cost_ms`       BIGINT       DEFAULT NULL COMMENT '执行耗时(毫秒)',
  `create_time`   DATETIME     DEFAULT NULL COMMENT '记录创建时间',
  `update_time`   DATETIME     DEFAULT NULL COMMENT '记录更新时间',
  `is_deleted`    INT          DEFAULT 0 COMMENT '是否删除（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  INDEX `idx_task_id` (`task_id`),
  INDEX `idx_fire_time` (`fire_time`),
  INDEX `idx_task_fire` (`task_id`, `fire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务执行记录表';
