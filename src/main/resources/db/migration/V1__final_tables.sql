-- 最终表结构脚本（已包含所有迁移变更）
-- 来源: d:/idealProjects/teakWeb2/src/main/resources/db/migration/
-- 生成时间: 2026-04-29
-- 说明: 此脚本包含所有表的最终CREATE TABLE语句，已应用所有ALTER TABLE变更
--       执行此脚本将直接创建最终表结构，无需执行额外的ALTER语句

-- ========================================================================
-- 表: device_fault_records
-- ========================================================================
CREATE TABLE `device_fault_records` (
  `record_id` BIGINT NOT NULL COMMENT '主键ID（非自增，需手动赋值）',
  `fault_id` VARCHAR(64) NOT NULL COMMENT '故障ID（业务唯一标识）',
  `device_id` BIGINT NOT NULL COMMENT '设备ID',
  `fault_type` INT NOT NULL COMMENT '故障类型枚举（1-硬件，2-软件）',
  `fault_level` INT NOT NULL COMMENT '故障级别枚举（1-低，2-中，3-高）',
  `occur_time` DATETIME(3) NOT NULL COMMENT '事件发生时间（含毫秒精度）',
  `op_type` INT NOT NULL COMMENT '操作类型枚举（1-故障产生，2-派单，3-修复）',
  PRIMARY KEY (`record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备故障记录表';

-- ========================================================================
-- 表: sys_scheduled_task（最终结构）
-- 应用了V12（添加task_args）和V16（删除params/parameter_types、添加唯一约束）
-- ========================================================================
CREATE TABLE `sys_scheduled_task` (
  `id` BIGINT NOT NULL COMMENT '主键',
  `task_name` VARCHAR(255) NOT NULL COMMENT '任务唯一标识',
  `cron_expression` VARCHAR(255) NOT NULL COMMENT 'cron表达式',
  `bean_name` VARCHAR(255) DEFAULT NULL COMMENT 'Spring Bean名称',
  `method_name` VARCHAR(255) NOT NULL COMMENT '执行方法',
  `task_args` TEXT DEFAULT NULL COMMENT '任务参数(JSON格式)。支持两种模式：
1. 位置参数模式：["value1", "value2"] - 按位置匹配方法参数
2. 命名参数模式：{"param1":"value1","param2":"value2"} - 按参数名匹配（需方法参数使用@Param注解）',
  `status` INT NOT NULL DEFAULT 1 COMMENT '状态: 0-停用, 1-启用',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `is_deleted` INT DEFAULT 0 COMMENT '是否删除（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_task_name` (`task_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态定时任务表';

-- ========================================================================
-- 表: sys_scheduled_task_log
-- ========================================================================
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

-- ========================================================================
-- 表: SPRING_SESSION（Spring Session 存储）
-- ========================================================================
CREATE TABLE SPRING_SESSION (
	PRIMARY_ID CHAR(36) NOT NULL,
	SESSION_ID CHAR(36) NOT NULL,
	CREATION_TIME BIGINT NOT NULL,
	LAST_ACCESS_TIME BIGINT NOT NULL,
	MAX_INACTIVE_INTERVAL INT NOT NULL,
	EXPIRY_TIME BIGINT NOT NULL,
	PRINCIPAL_NAME VARCHAR(100),
	CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC;

CREATE UNIQUE INDEX SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);
CREATE INDEX SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);
CREATE INDEX SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);

-- ========================================================================
-- 表: SPRING_SESSION_ATTRIBUTES（Spring Session 属性）
-- ========================================================================
CREATE TABLE SPRING_SESSION_ATTRIBUTES (
	SESSION_PRIMARY_ID CHAR(36) NOT NULL,
	ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
	ATTRIBUTE_BYTES BLOB NOT NULL,
	CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
	CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION(PRIMARY_ID) ON DELETE CASCADE
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC;

-- ========================================================================
-- 脚本结束
-- ========================================================================